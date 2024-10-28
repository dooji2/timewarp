package com.dooji.timewarp.data;

import com.dooji.timewarp.world.TimewarpArea;

import java.util.*;

public class TimewarpData {
    public Preferences preferences;
    public List<TimewarpArea> timewarpAreas;
    public Map<UUID, Map<String, Object>> playerData;

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
    }
}