package com.dooji.timewarp.data;

import com.dooji.timewarp.Timewarp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DataHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE_NAME = "timewarp_data.json";

    private static Path getDataFilePath(MinecraftServer server) {
        Path worldDir = server.getSavePath(WorldSavePath.ROOT);
        return worldDir.resolve(DATA_FILE_NAME);
    }

    public static void saveData(MinecraftServer server, TimewarpData data) {
        Path filePath = getDataFilePath(server);
        try {
            Files.createDirectories(filePath.getParent());
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                gson.toJson(data, writer);
            }

            if (Timewarp.DEBUG_MODE) {
                Timewarp.LOGGER.info("[Timewarp] Timewarp data saved to {}", filePath);
            }
        } catch (IOException e) {
            Timewarp.LOGGER.error("[Timewarp] Failed to save timewarp data", e);
        }
    }

    public static TimewarpData loadData(MinecraftServer server) {
        Path filePath = getDataFilePath(server);
        try (FileReader reader = new FileReader(filePath.toFile())) {
            TimewarpData data = gson.fromJson(reader, TimewarpData.class);

            if (data.getAutomaticObjectiveMechanics().isEmpty()) {
                data.setAutomaticObjectiveMechanics(getDefaultMechanics());
            }

            return data;
        } catch (IOException | JsonSyntaxException e) {
            Timewarp.LOGGER.error("[Timewarp] Failed to load timewarp data", e);

            TimewarpData defaultData = new TimewarpData();
            defaultData.setAutomaticObjectiveMechanics(getDefaultMechanics());
            return defaultData;
        }
    }

    private static Map<String, Boolean> getDefaultMechanics() {
        Map<String, Boolean> defaultMechanics = new HashMap<>();
        defaultMechanics.put("allowStacking", true);
        defaultMechanics.put("oldMinecart", true);
        defaultMechanics.put("oldAnimalBehavior", true);
        defaultMechanics.put("allowSprinting", true);
        defaultMechanics.put("versionText", true);
        defaultMechanics.put("oldGUI", true);
        defaultMechanics.put("noFrontView", true);
        defaultMechanics.put("noSneaking", true);
        defaultMechanics.put("noSwimming", true);
        defaultMechanics.put("oldCombat", true);
        defaultMechanics.put("noTrading", true);
        defaultMechanics.put("oldLook", true);
        defaultMechanics.put("noSmoothLighting", true);
        return defaultMechanics;
    }
}