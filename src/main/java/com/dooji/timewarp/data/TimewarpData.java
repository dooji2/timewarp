package com.dooji.timewarp.data;

import com.dooji.timewarp.world.TimewarpArea;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.*;

public class TimewarpData {
    public Preferences preferences;
    public List<TimewarpArea> timewarpAreas;
    public Map<UUID, Map<String, Object>> playerData;
    public Map<String, Boolean> automaticObjectiveMechanics = new HashMap<>();

    public TimewarpData() {
        preferences = new Preferences();
        timewarpAreas = new ArrayList<>();
        playerData = new HashMap<>();
    }

    public static class Preferences {
        public int shiftDurationMin = 1200;
        public int shiftDurationMax = 2400;
        public int timeUntilShiftMin = 1200;
        public int timeUntilShiftMax = 2400;
        public int saveInterval = 12000;
        public int opCommandLevel = 2;
        public boolean enableTriggering = true;
        public boolean debugMode = false;

        @JsonAdapter(ItemArrayAdapter.class)
        public Item[] items = {Items.EMERALD, Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT};
    }

    public Map<String, Boolean> getAutomaticObjectiveMechanics() {
        return automaticObjectiveMechanics;
    }

    public void setAutomaticObjectiveMechanics(Map<String, Boolean> automaticObjectiveMechanics) {
        this.automaticObjectiveMechanics = automaticObjectiveMechanics;
    }
}