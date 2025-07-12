package com.epherical.shoppy.mixin.client;


import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AccessorAbstractContainerScreen<T> {


    @Accessor("menu")
    AbstractContainerMenu shoppy$getMenu();

    @Accessor("menu")
    @Mutable
    void shoppy$setMenu(AbstractContainerMenu menu);


}
