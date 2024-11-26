package com.dooji.timewarp;

import com.dooji.timewarp.data.PausedTimewarpState;
import com.dooji.timewarp.network.TimewarpClientNetworking;
import com.dooji.timewarp.network.payloads.TimewarpSyncDataPayload;
import com.dooji.timewarp.ui.CustomToast;
import com.dooji.timewarp.world.TimewarpArea;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.dooji.timewarp.Timewarp.*;

public class TimewarpClient implements ClientModInitializer {
    private static TimewarpClient instance;
    private KeyBinding oldTexturesKey;

    private boolean oldTexturesKeyPressed = false;
    private boolean wasProgrammerArtEnabled = false;
    private boolean toastShownForOldLook = false;

    @Override
    public void onInitializeClient() {
        instance = this;

        oldTexturesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.timewarp.old_textures",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.timewarp"
        ));

        TimewarpClientNetworking.init();
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);

        if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
            TimewarpSyncDataPayload requestPayload = new TimewarpSyncDataPayload("");
            ClientPlayNetworking.send(requestPayload);
        }
    }

    public static TimewarpClient getInstance() {
        return instance;
    }

    private void onClientTick(MinecraftClient client) {
        if (!client.isIntegratedServerRunning() && client.player != null) {
            managePlayerShift(client.player);
            handlePlayerAreaFeatures(client.player);
        }

        if (oldTexturesKey.wasPressed()) {
            if (Timewarp.isRetroShiftActive(client.player) && Timewarp.getRetroSetting(client.player, "oldLook")) {
                oldTexturesKeyPressed = true;
                manageProgrammerArtPack();
            } else {
                oldTexturesKeyPressed = false;
            }
        }

        manageProgrammerArtPack();
    }

    public void handlePlayerAreaFeatures(PlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        TimewarpArea area = Timewarp.getInstance().getAreaContainingPosition(playerPos);

        if (area != null) {
            if (!area.equals(activePlayerAreas.get(player))) {
                if (retroShiftActive.getOrDefault(player, false)) {
                    pauseTimewarp(player);
                }

                activateAllFeatures(player, area);
                activePlayerAreas.put(player, area);

                player.sendMessage(Text.translatable("message.timewarp.entered_area", area.getName()), true);
            }
        } else if (activePlayerAreas.containsKey(player)) {
            deactivateAreaFeatures(player);
            player.sendMessage(Text.translatable("message.timewarp.left_area", activePlayerAreas.get(player).getName()), true);
            activePlayerAreas.remove(player);

            if (!retroShiftActive.getOrDefault(player, false)) {
                resumePausedTimewarp(player);
            }
        }
    }

    public void managePlayerShift(PlayerEntity player) {
        if (activePlayerAreas.containsKey(player)) {
            return;
        }

        if (retroShiftActive.getOrDefault(player, false)) {
            int shiftDuration = shiftDurationTimers.getOrDefault(player, SHIFT_DURATION_MIN);

            if (Timewarp.getInstance().checkObjectiveCompletion(player)) {
                endTimeShift(player);
                return;
            }

            if (shiftDuration <= 0) {
                endTimeShift(player);
            } else {
                shiftDurationTimers.put(player, shiftDuration - 1);
            }
        }
    }

    public void pauseTimewarp(PlayerEntity player) {
        PausedTimewarpState pausedState = new PausedTimewarpState(
                shiftDurationTimers.get(player),
                retroTimeShiftSettings.get(player),
                objectives.get(player),
                startingInventory.get(player)
        );

        pausedTimewarps.put(player, pausedState);

        retroShiftActive.put(player, false);
        shiftDurationTimers.remove(player);
        retroTimeShiftSettings.remove(player);
        objectives.remove(player);
        startingInventory.remove(player);
    }

    public void resumePausedTimewarp(PlayerEntity player) {
        if (pausedTimewarps.containsKey(player)) {
            PausedTimewarpState pausedState = pausedTimewarps.remove(player);

            shiftDurationTimers.put(player, pausedState.getShiftDurationRemaining());
            retroTimeShiftSettings.put(player, pausedState.getRetroSettings());
            objectives.put(player, pausedState.getObjective());
            startingInventory.put(player, pausedState.getStartingInventory());
            retroShiftActive.put(player, true);
        } else {
            resetTimer(player);
        }
    }

    private void activateAllFeatures(PlayerEntity player, TimewarpArea area) {
        Map<String, Boolean> settings = new HashMap<>();

        settings.put("allowStacking", area.getFeature("allowStacking"));
        settings.put("oldMinecart", area.getFeature("oldMinecart"));
        settings.put("oldAnimalBehavior", area.getFeature("oldAnimalBehavior"));
        settings.put("allowSprinting", area.getFeature("allowSprinting"));
        settings.put("versionText", area.getFeature("versionText"));
        settings.put("oldGUI", area.getFeature("oldGUI"));
        settings.put("noFrontView", area.getFeature("noFrontView"));
        settings.put("noSneaking", area.getFeature("noSneaking"));
        settings.put("noSwimming", area.getFeature("noSwimming"));
        settings.put("oldCombat", area.getFeature("oldCombat"));
        settings.put("noTrading", area.getFeature("noTrading"));
        settings.put("oldLook", area.getFeature("oldLook"));
        settings.put("noSmoothLighting", area.getFeature("noSmoothLighting"));

        retroShiftActive.put(player, true);
        retroTimeShiftSettings.put(player, settings);
    }

    public void deactivateAreaFeatures(PlayerEntity player) {
        retroShiftActive.put(player, false);
        retroTimeShiftSettings.remove(player);
    }

    public void triggerTimeShift(PlayerEntity player) {
        Identifier iconTexture = Identifier.of("minecraft", "textures/item/minecart.png");

        int shiftDuration;
        if (SHIFT_DURATION_MIN != SHIFT_DURATION_MAX) {
            shiftDuration = SHIFT_DURATION_MIN + random.nextInt(SHIFT_DURATION_MAX - SHIFT_DURATION_MIN);
        } else {
            shiftDuration = SHIFT_DURATION_MIN;
        }

        shiftDurationTimers.put(player, shiftDuration);

        createToast("message.timewarp.retro_title", getRandomMessage(), iconTexture);

        retroShiftActive.put(player, true);
        setRetroShiftSettings(player);
        generateObjective(player, shiftDuration);

        startingInventory.put(player, new ArrayList<>(player.getInventory().main));
    }

    private void setRetroShiftSettings(PlayerEntity player) {
        retroTimeShiftSettings.put(player, new HashMap<>() {{
            put("allowStacking", automaticObjectiveMechanics.getOrDefault("allowStacking", false) ? random.nextBoolean() : false);
            put("oldMinecart", automaticObjectiveMechanics.getOrDefault("oldMinecart", false) ? random.nextBoolean() : false);
            put("oldAnimalBehavior", automaticObjectiveMechanics.getOrDefault("oldAnimalBehavior", false) ? random.nextBoolean() : false);
            put("allowSprinting", automaticObjectiveMechanics.getOrDefault("allowSprinting", false) ? random.nextBoolean() : false);
            put("versionText", automaticObjectiveMechanics.getOrDefault("versionText", false) ? random.nextBoolean() : false);
            put("oldGUI", automaticObjectiveMechanics.getOrDefault("oldGUI", false) ? random.nextBoolean() : false);
            put("noFrontView", automaticObjectiveMechanics.getOrDefault("noFrontView", false) ? random.nextBoolean() : false);
            put("noSneaking", automaticObjectiveMechanics.getOrDefault("noSneaking", false) ? random.nextBoolean() : false);
            put("noSwimming", automaticObjectiveMechanics.getOrDefault("noSwimming", false) ? random.nextBoolean() : false);
            put("oldCombat", automaticObjectiveMechanics.getOrDefault("oldCombat", false) ? random.nextBoolean() : false);
            put("noTrading", automaticObjectiveMechanics.getOrDefault("noTrading", false) ? random.nextBoolean() : false);
            put("oldLook", automaticObjectiveMechanics.getOrDefault("oldLook", false) ? random.nextBoolean() : false);
            put("noSmoothLighting", automaticObjectiveMechanics.getOrDefault("noSmoothLighting", false) ? random.nextBoolean() : false);
        }});
    }

    private void generateObjective(PlayerEntity player, int shiftDuration) {
        Item[] items = Timewarp.getInstance().ITEMS;
        Item objectiveItem = items[random.nextInt(items.length)];
        int amount = 1 + random.nextInt(3);

        Map<Item, Integer> objective = new HashMap<>();
        objective.put(objectiveItem, amount);
        objectives.put(player, objective);


        Map<String, Boolean> features = retroTimeShiftSettings.get(player);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && !MinecraftClient.getInstance().isIntegratedServerRunning() && MinecraftClient.getInstance().player != null) {
            TimewarpClientNetworking.sendData(player.getUuid(), objective, features, shiftDuration);
        }
    }

    public void endTimeShift(PlayerEntity player) {
        retroShiftActive.put(player, false);
        Identifier iconTexture = Identifier.of("minecraft", "textures/item/minecart.png");

        createToast("message.timewarp.present_title", "message.timewarp.present_message", iconTexture);

        Timewarp.getInstance().resetShiftSettings(player);
        objectives.remove(player);
        startingInventory.remove(player);

        oldTexturesKeyPressed = false;
        toastShownForOldLook = false;
    }

    void handleClientCornerSelection(UUID playerId, BlockPos corner1, BlockPos corner2) {
        Timewarp.getInstance().storeCornerSelection(playerId, corner1, corner2);
    }

    private void resetTimer(PlayerEntity player) {
        if (TIME_UNTIL_SHIFT_MIN != TIME_UNTIL_SHIFT_MAX) {
            playerShiftTimers.put(player, TIME_UNTIL_SHIFT_MIN + random.nextInt(TIME_UNTIL_SHIFT_MAX - TIME_UNTIL_SHIFT_MIN));
        } else {
            playerShiftTimers.put(player, TIME_UNTIL_SHIFT_MIN);
        }
    }

    private String getRandomMessage() {
        String[] messageKeys = {
                "message.timewarp.random_message_1",
                "message.timewarp.random_message_2",
                "message.timewarp.random_message_3",
                "message.timewarp.random_message_4",
                "message.timewarp.random_message_5"
        };
        return Text.translatable(messageKeys[random.nextInt(messageKeys.length)]).getString();
    }

    public static void createToast(String titleKey, String messageKey, Identifier iconTexture) {
        MinecraftClient client = MinecraftClient.getInstance();
        ToastManager toastManager = client.getToastManager();

        Identifier backgroundTexture = Identifier.of(Timewarp.MOD_ID, "textures/gui/toast.png");
        CustomToast customToast = new CustomToast(
                Text.translatable(titleKey),
                Text.translatable(messageKey),
                5000,
                0xFFFFFF,
                0xAAAAAA,
                backgroundTexture,
                iconTexture,
                16,
                160,
                32
        );
        toastManager.add(customToast);
    }

    public void manageProgrammerArtPack() {
        MinecraftClient client = MinecraftClient.getInstance();
        ResourcePackManager resourcePackManager = client.getResourcePackManager();
        ResourcePackProfile programmerArtPack = resourcePackManager.getProfile("programmer_art");

        if (client.player != null && programmerArtPack != null) {
            boolean shouldEnablePack = Timewarp.isRetroShiftActive(client.player) && Timewarp.getRetroSetting(client.player, "oldLook");

            if (shouldEnablePack) {
                if (!oldTexturesKeyPressed && !toastShownForOldLook) {
                    String keyName = oldTexturesKey.getBoundKeyLocalizedText().getString();
                    Identifier iconTexture = Identifier.of("minecraft", "textures/item/minecart.png");
                    createToast("message.timewarp.request_textures", Text.translatable("message.timewarp.press_key_to_enable", keyName).getString(), iconTexture);

                    toastShownForOldLook = true;
                }

                if (oldTexturesKeyPressed) {
                    boolean isProgrammerArtEnabled = client.options.resourcePacks.contains("programmer_art");

                    if (!isProgrammerArtEnabled) {
                        client.options.resourcePacks.add("programmer_art");
                        wasProgrammerArtEnabled = true;
                        resourcePackManager.scanPacks();
                        client.options.addResourcePackProfilesToManager(resourcePackManager);
                        client.reloadResources();

                        Identifier iconTexture = Identifier.of("minecraft", "textures/item/minecart.png");
                        createToast("message.timewarp.textures_enabled", "message.timewarp.old_textures_active", iconTexture);
                    }
                }
            } else if (!shouldEnablePack && wasProgrammerArtEnabled) {
                client.options.resourcePacks.remove("programmer_art");
                wasProgrammerArtEnabled = false;
                resourcePackManager.scanPacks();
                client.options.addResourcePackProfilesToManager(resourcePackManager);
                client.reloadResources();

                toastShownForOldLook = false;
            }
        }
    }
}