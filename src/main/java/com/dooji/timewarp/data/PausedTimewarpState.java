package com.dooji.timewarp.data;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

public class PausedTimewarpState {
    private final int shiftDurationRemaining;
    private final Map<String, Boolean> retroSettings;
    private final Map<Item, Integer> objective;
    private final List<ItemStack> startingInventory;

    public PausedTimewarpState(int shiftDurationRemaining, Map<String, Boolean> retroSettings, Map<Item, Integer> objective, List<ItemStack> startingInventory) {
        this.shiftDurationRemaining = shiftDurationRemaining;
        this.retroSettings = new HashMap<>(retroSettings);
        this.objective = new HashMap<>(objective);
        this.startingInventory = new ArrayList<>(startingInventory);
    }

    public int getShiftDurationRemaining() {
        return shiftDurationRemaining;
    }

    public Map<String, Boolean> getRetroSettings() {
        return retroSettings;
    }

    public Map<Item, Integer> getObjective() {
        return objective;
    }

    public List<ItemStack> getStartingInventory() {
        return startingInventory;
    }
}
