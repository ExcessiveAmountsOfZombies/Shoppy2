package com.epherical.shoppy.menu.bartering;

import com.epherical.shoppy.Shoppy;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jetbrains.annotations.Nullable;

public class BarteringMenuOwner extends BarteringMenu {

    public boolean editing;

    public BarteringMenuOwner(int pContainerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(Shoppy.BARTERING_MENU_OWNER.get(), pContainerId, extraData.readBlockPos(), extraData.readBoolean(), new SimpleContainerData(2));
    }

    public BarteringMenuOwner(@Nullable MenuType<?> pMenuType, int pContainerId, BlockPos blockPos, boolean editing, ContainerData containerData) {
        super(pMenuType, pContainerId, blockPos, containerData);
        this.editing = editing;
    }


    public static BarteringMenuOwner barteringOwner(int pContainerId, BlockPos blockPos, boolean editing, ContainerData data) {
        return new BarteringMenuOwner(Shoppy.BARTERING_MENU_OWNER.get(), pContainerId, blockPos, editing, data);
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
