package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.economy.EconomyBridge;
import com.epherical.shoppy.economy.EconomyBridgeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

public class TradingBlockEntity extends BarteringBlockEntity {

    private final double[] moneyPrices = new double[3];
    private boolean buyingFromPlayers;

    public TradingBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Shoppy.TRADING_SHOP_ENTITY.get(), blockPos, blockState);
    }

    public TradingBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    protected @Nullable Component getDefaultName() {
        return Component.translatable("block.shoppy.trading_shop").setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
    }

    @Override
    public boolean usesItemCurrency() {
        return false;
    }

    @Override
    public void setOffer(int idx, double price, int sale) {
        super.setOffer(idx, price, sale);
        if (idx < 0 || idx >= moneyPrices.length) {
            return;
        }
        moneyPrices[idx] = Math.max(0.0D, price);
    }

    @Override
    public double getOfferPrice(int idx) {
        if (idx < 0 || idx >= moneyPrices.length) {
            return 0.0D;
        }
        return moneyPrices[idx];
    }

    @Override
    public String getOfferPriceText(int idx) {
        return String.format(Locale.ROOT, "$%.2f", getOfferPrice(idx));
    }

    @Override
    public int getCurrencyItemCount() {
        return 0;
    }

    @Override
    public void addCurrencyItems(int n) {
        // Account-currency shops do not store item currency.
    }

    @Override
    public void handleTradeAttempt(Player player, int offerIdx) {
        if (isBuyingFromPlayers()) {
            tryBuyFromPlayer(player, offerIdx);
        } else {
            trySellToPlayer(player, offerIdx);
        }
    }

    @Override
    public boolean supportsTradeDirectionToggle() {
        return true;
    }

    @Override
    public boolean isBuyingFromPlayers() {
        return buyingFromPlayers;
    }

    @Override
    public void setBuyingFromPlayers(boolean value) {
        buyingFromPlayers = value;
    }

    @Override
    public void toggleTradeDirection() {
        buyingFromPlayers = !buyingFromPlayers;
    }

    private void trySellToPlayer(Player player, int offerIdx) {
        if (offerIdx < 0 || offerIdx > 2) {
            return;
        }

        int saleQty = getSaleCount(offerIdx);
        double price = getOfferPrice(offerIdx);

        if (saleQty <= 0 || price <= 0.0D) {
            return;
        }

        if (getSaleItem().isEmpty()) {
            return;
        }

        if (!hasUnlimitedStock() && getSaleItemCount() < saleQty) {
            return;
        }

        EconomyBridge economy = EconomyBridgeManager.getBridge();
        if (!economy.isAvailable()) {
            player.sendSystemMessage(Component.translatable("shop.error.no_economy"));
            return;
        }

        UUID ownerId = getOwner();

        if (!economy.transfer(player.getUUID(), ownerId, price, "shop.purchase.transaction")) {
            player.sendSystemMessage(Component.translatable("shop.purchase.not_enough_money"));
            return;
        }

        ItemStack purchased = getSaleItem().copy();
        purchased.setCount(saleQty);
        ItemHandlerHelper.giveItemToPlayer(player, purchased);

        removeSaleItems(saleQty);
        setChanged();

        player.sendSystemMessage(Component.translatable("shop.purchase.transaction_success", purchased.getHoverName()));
    }

    private void tryBuyFromPlayer(Player player, int offerIdx) {
        if (offerIdx < 0 || offerIdx > 2) {
            return;
        }

        int quantity = getSaleCount(offerIdx);
        double payout = getOfferPrice(offerIdx);
        ItemStack template = getSaleItem();
        if (quantity <= 0 || payout <= 0.0D || template.isEmpty()) {
            return;
        }

        if (!hasUnlimitedStock() && getFreeSlots() < quantity) {
            player.sendSystemMessage(Component.translatable("shop.buying.full"));
            return;
        }

        if (countMatchingItems(player, template) < quantity) {
            player.sendSystemMessage(Component.translatable("shop.buying.not_enough_items"));
            return;
        }

        EconomyBridge economy = EconomyBridgeManager.getBridge();
        if (!economy.isAvailable()) {
            player.sendSystemMessage(Component.translatable("shop.error.no_economy"));
            return;
        }

        UUID ownerId = getOwner();

        if (!economy.hasFunds(ownerId, payout, "shop.buying.transaction.check")) {
            player.sendSystemMessage(Component.translatable("shop.buying.not_enough_funds"));
            return;
        }

        if (!removeMatchingItems(player, template, quantity)) {
            player.sendSystemMessage(Component.translatable("shop.buying.not_enough_items"));
            return;
        }

        if (!economy.transfer(ownerId, player.getUUID(), payout, "shop.buying.transaction")) {
            ItemStack rollback = template.copy();
            rollback.setCount(quantity);
            ItemHandlerHelper.giveItemToPlayer(player, rollback);
            player.sendSystemMessage(Component.translatable("shop.buying.not_enough_funds"));
            return;
        }

        addSaleItems(quantity);
        setChanged();

        Component moneyText = Component.translatable("text.shoppy.money_value", getOfferPriceText(offerIdx));
        ItemStack soldStack = template.copy();
        soldStack.setCount(quantity);
        player.sendSystemMessage(Component.translatable("shop.buying.player.success", soldStack.getHoverName(), moneyText));

        if (player.level().getServer() != null) {
            ServerPlayer ownerPlayer = player.level().getServer().getPlayerList().getPlayer(ownerId);
            if (ownerPlayer != null) {
                ownerPlayer.sendSystemMessage(Component.translatable("shop.buying.owner.success",
                        player.getDisplayName(), soldStack.getHoverName(), moneyText));
            }
        }
    }

    private static int countMatchingItems(Player player, ItemStack template) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItem(stack, template)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static boolean removeMatchingItems(Player player, ItemStack template, int required) {
        int remaining = required;
        for (ItemStack stack : player.getInventory().items) {
            if (!ItemStack.isSameItem(stack, template)) {
                continue;
            }

            int take = Math.min(stack.getCount(), remaining);
            stack.shrink(take);
            remaining -= take;
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void writeNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeNBT(tag, registries);
        ListTag prices = new ListTag();
        for (double moneyPrice : moneyPrices) {
            prices.add(DoubleTag.valueOf(moneyPrice));
        }
        tag.put("MoneyPrices", prices);
        tag.putBoolean("BuyingFromPlayers", buyingFromPlayers);
    }

    @Override
    protected void readNBT(CompoundTag tag, HolderLookup.Provider registries) {
        super.readNBT(tag, registries);
        buyingFromPlayers = tag.getBoolean("BuyingFromPlayers");
        for (int i = 0; i < moneyPrices.length; i++) {
            moneyPrices[i] = 0.0D;
        }
        if (!tag.contains("MoneyPrices", Tag.TAG_LIST)) {
            return;
        }

        ListTag loaded = tag.getList("MoneyPrices", Tag.TAG_DOUBLE);
        int len = Math.min(moneyPrices.length, loaded.size());
        for (int i = 0; i < len; i++) {
            moneyPrices[i] = loaded.getDouble(i);
        }
    }
}
