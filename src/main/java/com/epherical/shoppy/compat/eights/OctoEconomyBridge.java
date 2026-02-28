package com.epherical.shoppy.compat.eights;

import com.epherical.octoecon.api.OctoEconomy;
import com.epherical.octoecon.api.user.FakeUser;
import com.epherical.octoecon.api.user.UniqueUser;
import com.epherical.octoecon.api.user.User;
import com.epherical.shoppy.economy.EconomyBridge;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class OctoEconomyBridge implements EconomyBridge {

    private final OctoEconomy<? extends UniqueUser, ? extends FakeUser> economy;
    private final UniqueUser adminUser;

    public OctoEconomyBridge(OctoEconomy<? extends UniqueUser, ? extends FakeUser> economy) {
        this.economy = economy;
        adminUser = new UniqueUser() {
            @Override
            public UUID getUserID() {
                return Util.NIL_UUID;
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("economy.shoppy.admin_user");
            }

            @Override
            public double getBalance(String s) {
                return Double.MAX_VALUE;
            }

            @Override
            public boolean hasAmount(double v, String s) {
                return true;
            }

            @Override
            public void resetBalance(String s) {}

            @Override
            public void setBalance(double v, String s) {}

            @Override
            public void sendTo(User user, double v, String s) {}

            @Override
            public void depositMoney(double v, String s) {}

            @Override
            public void withdrawMoney(double v, String s) {}

            @Override
            public String getIdentity() {
                return getUserID().toString();
            }

            @Override
            public boolean isDirty() {
                return false;
            }
        };
    }

    @Override
    public boolean isAvailable() {
        return economy != null && economy.enabled();
    }

    @Override
    public double getBalance(UUID playerId, String reason) {
        if (!isAvailable()) {
            return 0.0D;
        }
        if (isAdminAccount(playerId)) {
            return Double.MAX_VALUE;
        }
        return getAccount(playerId).getBalance(reason);
    }

    @Override
    public boolean hasFunds(UUID playerId, double amount, String reason) {
        if (!isAvailable()) {
            return false;
        }
        if (isAdminAccount(playerId)) {
            return true;
        }
        return getAccount(playerId).hasAmount(amount, reason);
    }

    @Override
    public boolean transfer(UUID from, UUID to, double amount, String reason) {
        if (!isAvailable()) {
            return false;
        }

        if (amount <= 0.0D) {
            return true;
        }

        boolean fromAdmin = isAdminAccount(from);
        boolean toAdmin = isAdminAccount(to);

        if (!fromAdmin) {
            UniqueUser sender = getAccount(from);
            if (!sender.hasAmount(amount, reason)) {
                return false;
            }
            if (toAdmin) {
                sender.withdrawMoney(amount, reason);
                return true;
            }
            sender.sendTo(getAccount(to), amount, reason);
            return true;
        }

        // Admin sender has infinite funds but balance never changes.
        if (!toAdmin) {
            getAccount(to).depositMoney(amount, reason);
        }
        return true;
    }

    private static boolean isAdminAccount(UUID playerId) {
        return Util.NIL_UUID.equals(playerId);
    }

    private UniqueUser getAccount(UUID playerId) {
        return isAdminAccount(playerId) ? adminUser : economy.getOrCreatePlayerAccount(playerId);
    }
}
