package com.epherical.shoppy.block.entity;

import java.util.UUID;

public interface Ownable {

    void setOwner(UUID owner);

    UUID getOwner();
}
