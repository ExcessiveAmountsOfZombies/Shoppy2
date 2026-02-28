package com.epherical.shoppy.compat.eights;

import com.epherical.eights.event.NeoForgeEconomyProviderPostEvent;
import com.epherical.shoppy.economy.EconomyBridgeManager;
import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

public final class EightsEconomyCompat {

    public static final String MOD_ID = "eights_economy_p";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean registered;

    private EightsEconomyCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(EightsEconomyCompat::onEconomySet);
    }

    private static void onEconomySet(NeoForgeEconomyProviderPostEvent event) {
        EconomyBridgeManager.setBridge(new OctoEconomyBridge(event.getEconomy()));
        LOGGER.info("Shoppy is now using {} economy provider", event.getEconomy());
    }
}
