package com.epherical.shoppy;

import com.epherical.shoppy.block.BarteringBlock;
import com.epherical.shoppy.block.CreativeBarteringBlock;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeBarteringBlockEntity;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.network.payloads.AddItemRequestPayload;
import com.epherical.shoppy.network.ServerPayloadHandler;
import com.epherical.shoppy.network.payloads.PriceSubmissionPayload;
import com.epherical.shoppy.network.payloads.SetSaleItemPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Shoppy.MODID)
public class Shoppy {
    public static final String MODID = "shoppy";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);

    public static final DeferredBlock<BarteringBlock> BARTERING_STATION = BLOCKS.register("bartering_station",
            () -> new BarteringBlock(BlockBehaviour.Properties.of().strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredItem<BlockItem> BARTERING_STATION_ITEM = ITEMS.registerSimpleBlockItem(BARTERING_STATION);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BarteringBlockEntity>> BARTERING_STATION_ENTITY =
            BLOCK_ENTITIES.register("bartering_station", () ->
                    BlockEntityType.Builder.of(BarteringBlockEntity::new, BARTERING_STATION.get()).build(null));



    public static final DeferredBlock<CreativeBarteringBlock> CREATIVE_BARTERING_STATION = BLOCKS.register("creative_bartering_station",
            () -> new CreativeBarteringBlock(BlockBehaviour.Properties.of().strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredItem<BlockItem> CREATIVE_BARTERING_STATION_ITEM = ITEMS.registerSimpleBlockItem(CREATIVE_BARTERING_STATION);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeBarteringBlockEntity>> CREATIVE_BARTERING_STATION_ENTITY =
            BLOCK_ENTITIES.register("creative_bartering_station", () ->
                    BlockEntityType.Builder.of(CreativeBarteringBlockEntity::new, CREATIVE_BARTERING_STATION.get()).build(null));



    public static final DeferredHolder<MenuType<?>, MenuType<BarteringMenu>> BARTERING_MENU =
            MENU_TYPES.register("bartering_menu", () -> new MenuType<>(
                    (IContainerFactory<BarteringMenu>) BarteringMenu::new, FeatureFlags.VANILLA_SET));

    public static DeferredHolder<MenuType<?>, MenuType<BarteringMenuOwner>> BARTERING_MENU_OWNER =
            MENU_TYPES.register("bartering_menu_owner", () -> new MenuType<>(
                    (IContainerFactory<BarteringMenuOwner>) BarteringMenuOwner::new, FeatureFlags.VANILLA_SET));

    public static final PermissionNode<Boolean> ADMIN_BREAK = new PermissionNode<>("shoppy", "admin.break_shop", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> {
        return player != null && player.hasPermissions(4);
    });


    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.shoppy"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> BARTERING_STATION_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
        output.accept(BARTERING_STATION.get());
        output.accept(CREATIVE_BARTERING_STATION_ITEM.get());
    }).build());



    public Shoppy(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onCapability);
        modEventBus.addListener(this::registerNetworkPayloads);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    public void onCapability(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, context) -> {
            BarteringBlockEntity barteringBlockEntity = (BarteringBlockEntity) blockEntity;
            NonNullList<ItemStack> inventory = barteringBlockEntity.getInventory();
            return new ItemStackHandler(inventory) {
                @Override
                public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                    return switch (slot) {
                        case 0 -> ItemStack.isSameItem(stack, barteringBlockEntity.getSaleItem());     // only sale item may be inserted
                        case 1 -> false;                                                               // revenue slot never accepts input
                        default -> false;
                    };
                }

                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (slot == 1 && barteringBlockEntity.getCurrencyItemCount() > 0 && amount > 0) {
                        int taken = Math.min(amount, barteringBlockEntity.getCurrencyItemCount());
                        if (!simulate) {
                            barteringBlockEntity.addCurrencyItems(-taken);
                        }
                        // todo; figure out the curreny...
                        ItemStack out = barteringBlockEntity.getCurrencyItem().copy();
                        out.setCount(taken);
                        return out;
                    }
                    return ItemStack.EMPTY;


                }

                @Override
                public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                    if (slot == 0) {
                        if (stack.isEmpty()) return ItemStack.EMPTY;
                        if (barteringBlockEntity.getSaleItem().isEmpty() && !ItemStack.isSameItem(stack, barteringBlockEntity.getSaleItem())) return stack;
                        int free = 100 - barteringBlockEntity.getSaleItemCount();
                        if (free <= 0) return stack;            // stock full

                        int toInsert = Math.min(free, stack.getCount());
                        if (!simulate) {
                            barteringBlockEntity.addSaleItems(toInsert);
                        }

                        return stack.copyWithCount(stack.getCount() - toInsert);
                    }

                    /* slot 1 refuses insertion */
                    return stack;

                }
            };
        }, BARTERING_STATION.get());
    }


    public void registerNetworkPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN);
        registrar.playToServer(AddItemRequestPayload.TYPE, AddItemRequestPayload.STREAM_CODEC, ServerPayloadHandler::handle);
        registrar.playToServer(SetSaleItemPayload.TYPE, SetSaleItemPayload.STREAM_CODEC, ServerPayloadHandler::handle);
        registrar.playToServer(PriceSubmissionPayload.TYPE, PriceSubmissionPayload.STREAM_CODEC, ServerPayloadHandler::handle);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }


    @SubscribeEvent
    public void registerPermissions(PermissionGatherEvent.Nodes event) {
        event.addNodes(ADMIN_BREAK);
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            Player player = event.getEntity();
            BlockPos pos = event.getPos();
            ServerLevel level = (ServerLevel) event.getEntity().level();
            BlockEntity entity = level.getBlockEntity(pos);
            /*if (entity instanceof AbstractTradingBlockEntity trading) {
                if ((!trading.getOwner().equals(player.getUUID())) && (!player.hasPermissions(4) || PermissionAPI.getPermission((ServerPlayer) player, ADMIN_BREAK))) {
                    event.setCanceled(true);
                }
            }*/
        }
    }
}
