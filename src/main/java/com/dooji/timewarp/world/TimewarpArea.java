package com.dooji.timewarp.world;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimewarpArea {
    private final BlockPos corner1;
    private final BlockPos corner2;
    private final int areaId;
    private final Map<String, Boolean> features;
    private final UUID owner;
    private final String name;

    public TimewarpArea(BlockPos corner1, BlockPos corner2, UUID owner, String name, int id) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.areaId = id;
        this.features = new HashMap<>();
        this.owner = owner;
        this.name = name;

        features.put("allowStacking", false);
        features.put("oldMinecart", true);
        features.put("oldAnimalBehavior", true);
        features.put("allowSprinting", false);
        features.put("versionText", true);
        features.put("oldGUI", true);
        features.put("noFrontView", true);
        features.put("noSneaking", true);
        features.put("noSwimming", true);
        features.put("oldCombat", true);
        features.put("noTrading", true);
    }

    public int getId() {
        return areaId;
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= Math.min(corner1.getX(), corner2.getX()) &&
                pos.getX() <= Math.max(corner1.getX(), corner2.getX()) &&
                pos.getY() >= Math.min(corner1.getY(), corner2.getY()) &&
                pos.getY() <= Math.max(corner1.getY(), corner2.getY()) &&
                pos.getZ() >= Math.min(corner1.getZ(), corner2.getZ()) &&
                pos.getZ() <= Math.max(corner1.getZ(), corner2.getZ());
    }

    public void setFeature(String feature, boolean enabled) {
        features.put(feature, enabled);
    }

    public boolean getFeature(String feature) {
        return features.getOrDefault(feature, false);
    }

    public BlockPos getCorner1() {
        return corner1;
    }

    public BlockPos getCorner2() {
        return corner2;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }
}