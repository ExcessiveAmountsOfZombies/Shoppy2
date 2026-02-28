package com.epherical.shoppy.economy;

import java.util.UUID;

public interface EconomyBridge {

    EconomyBridge UNAVAILABLE = new EconomyBridge() {
        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public double getBalance(UUID playerId, String reason) {
            return 0.0D;
        }

        @Override
        public boolean hasFunds(UUID playerId, double amount, String reason) {
            return false;
        }

        @Override
        public boolean transfer(UUID from, UUID to, double amount, String reason) {
            return false;
        }
    };

    boolean isAvailable();

    double getBalance(UUID playerId, String reason);

    boolean hasFunds(UUID playerId, double amount, String reason);

    boolean transfer(UUID from, UUID to, double amount, String reason);
}
