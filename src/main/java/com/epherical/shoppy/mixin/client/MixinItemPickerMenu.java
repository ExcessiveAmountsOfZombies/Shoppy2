package com.epherical.shoppy.mixin.client;


import com.epherical.shoppy.client.ShoppyCreativeSlot;
import com.epherical.shoppy.client.CustomItemPickerMenu;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public class MixinItemPickerMenu {


    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 0))
    public Slot shoppy$useMySlots(CreativeModeInventoryScreen.ItemPickerMenu instance, Slot slot) {
        AccessorAbstractContainerMenu menu = (AccessorAbstractContainerMenu) instance;
        if (instance instanceof CustomItemPickerMenu) {
            ShoppyCreativeSlot shoppyCreativeSlot = new ShoppyCreativeSlot(AccessorCreativeModeInventoryScreen.getContainer(), slot.getSlotIndex(), slot.x, slot.y);
            menu.invokeAddSlot(shoppyCreativeSlot);
            return shoppyCreativeSlot;
        } else {
            menu.invokeAddSlot(slot);
        }
        return slot;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;addInventoryHotbarSlots(Lnet/minecraft/world/Container;II)V"))
    public void shoppy$useMySlotsStill(CreativeModeInventoryScreen.ItemPickerMenu instance, Container container, int x, int y) {
        AccessorAbstractContainerMenu menu = (AccessorAbstractContainerMenu) instance;
        if (instance instanceof CustomItemPickerMenu) {
            for (int i = 0; i < 9; i++) {
                menu.invokeAddSlot(new ShoppyCreativeSlot(container, i, x + i * 18, y));
            }
        } else {
            for (int i = 0; i < 9; i++) {
                menu.invokeAddSlot(new Slot(container, i, x + i * 18, y));
            }
        }
    }
}
