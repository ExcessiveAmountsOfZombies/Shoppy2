package com.epherical.shoppy;

import com.epherical.shoppy.client.render.BarteringBlockRenderer;
import com.epherical.shoppy.client.screens.BarteringScreen;
import com.epherical.shoppy.client.screens.BarteringScreenOwner;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import static com.epherical.shoppy.Shoppy.MODID;
import static com.epherical.shoppy.client.ShoppyClient.tick;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ShoppyClient {


    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Shoppy.BARTERING_STATION.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(Shoppy.CREATIVE_BARTERING_STATION.get(), RenderType.cutout());
    }


    @SubscribeEvent
    public static void registerScreen(RegisterMenuScreensEvent event) {
        event.register(Shoppy.BARTERING_MENU.get(), BarteringScreen::new);
        event.register(Shoppy.BARTERING_MENU_OWNER.get(), BarteringScreenOwner::new);
    }


    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Shoppy.BARTERING_STATION_ENTITY.get(), BarteringBlockRenderer::new);
        event.registerBlockEntityRenderer(Shoppy.CREATIVE_BARTERING_STATION_ENTITY.get(), BarteringBlockRenderer::new);
    }


    @SubscribeEvent
    public static void onEndTick(ClientTickEvent.Post event) {
        tick++;
        if (tick == Integer.MAX_VALUE) {
            tick = Integer.MIN_VALUE;
        }
    }
}
