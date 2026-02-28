package com.epherical.shoppy.economy;

public final class EconomyBridgeManager {

    private static volatile EconomyBridge bridge = EconomyBridge.UNAVAILABLE;

    private EconomyBridgeManager() {
    }

    public static EconomyBridge getBridge() {
        return bridge;
    }

    public static void setBridge(EconomyBridge newBridge) {
        bridge = newBridge == null ? EconomyBridge.UNAVAILABLE : newBridge;
    }

    public static boolean isAvailable() {
        return bridge.isAvailable();
    }
}
