package com.epherical.shoppy.menu.bartering;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class BarteringMenuOwner extends BarteringMenu {

    public boolean editing;

    public BarteringMenuOwner(int pContainerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(Shoppy.BARTERING_MENU_OWNER.get(), pContainerId, extraData.readBlockPos(), extraData.readBoolean());
    }

    public BarteringMenuOwner(@Nullable MenuType<?> pMenuType, int pContainerId, BlockPos blockPos, boolean editing) {
        super(pMenuType, pContainerId, blockPos);
        this.editing = editing;
    }


    public static BarteringMenuOwner barteringOwner(int pContainerId, BlockPos blockPos, boolean editing) {
        return new BarteringMenuOwner(Shoppy.BARTERING_MENU_OWNER.get(), pContainerId, blockPos, editing);
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
