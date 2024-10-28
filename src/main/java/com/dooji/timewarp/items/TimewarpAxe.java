package com.dooji.timewarp.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class TimewarpAxe extends Item {
    private static final Map<PlayerEntity, BlockPos[]> selections = new HashMap<>();

    public TimewarpAxe(Settings settings) {
        super(settings);
    }

    public static BlockPos[] getSelection(PlayerEntity player) {
        return selections.computeIfAbsent(player, k -> new BlockPos[2]);
    }

    public static void clearSelection(PlayerEntity player) {
        selections.remove(player);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}