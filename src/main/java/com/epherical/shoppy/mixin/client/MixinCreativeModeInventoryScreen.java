package com.epherical.shoppy.mixin.client;

import com.epherical.shoppy.client.CustomItemPickerMenu;
import com.epherical.shoppy.client.screens.ShopPickingCreativeInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen {


    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void shoppy$redirectConstructor(LocalPlayer player, FeatureFlagSet enabledFeatures, boolean displayOperatorCreativeTab, CallbackInfo ci) {
        if ((Object) this instanceof ShopPickingCreativeInventoryScreen) {
            CustomItemPickerMenu customItemPickerMenu = new CustomItemPickerMenu(player);
            player.containerMenu = customItemPickerMenu;

            AccessorAbstractContainerScreen<?> accessor = (AccessorAbstractContainerScreen<?>) (Object) this;
            accessor.shoppy$setMenu(customItemPickerMenu);
        }
    }


    @Redirect(method = "containerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"))
    public boolean shoppy$redirectInfiniteItems(MultiPlayerGameMode instance) {
        if ((Object) this instanceof ShopPickingCreativeInventoryScreen) {
            return true;
        } else {
            return instance.hasInfiniteItems();
        }
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"))
    public boolean shoppy$redirectInitInfiniteItems(MultiPlayerGameMode instance) {
        if ((Object) this instanceof ShopPickingCreativeInventoryScreen) {
            return true;
        } else {
            return instance.hasInfiniteItems();
        }
    }


}
