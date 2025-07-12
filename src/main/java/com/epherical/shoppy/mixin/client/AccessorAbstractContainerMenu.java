package com.epherical.shoppy.mixin.client;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerMenu.class)
public interface AccessorAbstractContainerMenu {

    @Invoker("addSlot")
    Slot invokeAddSlot(Slot slot);


}
