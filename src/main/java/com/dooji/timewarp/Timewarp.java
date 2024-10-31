package com.dooji.timewarp;

import com.dooji.timewarp.commands.TimewarpCommands;
import com.dooji.timewarp.data.DataHandler;
import com.dooji.timewarp.data.PausedTimewarpState;
import com.dooji.timewarp.data.TimewarpData;
import com.dooji.timewarp.items.TimewarpAxe;
import com.dooji.timewarp.network.TimewarpClientNetworking;
import com.dooji.timewarp.network.TimewarpNetworking;
import com.dooji.timewarp.world.TimewarpArea;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Timewarp implements ModInitializer {
    public static final String MOD_ID = "timewarp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static Timewarp instance;

    public static final Item TIMEWARP_AXE = new TimewarpAxe(new Item.Settings().maxCount(1));

    public static int SHIFT_DURATION_MIN;
    public static int SHIFT_DURATION_MAX;
    public static int TIME_UNTIL_SHIFT_MIN;
    public static int TIME_UNTIL_SHIFT_MAX;
    public static int MIN_OP_LEVEL;
    public static int SAVE_INTERVAL;
    public static boolean ENABLE_TRIGGERING;

    static final Random random = new Random();

    static final List<TimewarpArea> timewarpAreas = new ArrayList<>();
    static final Map<PlayerEntity, TimewarpArea> activePlayerAreas = new HashMap<>();
    static final Map<PlayerEntity, Integer> playerShiftTimers = new HashMap<>();
    static final Map<PlayerEntity, Integer> shiftDurationTimers = new HashMap<>();
    static final Map<PlayerEntity, Boolean> retroShiftActive = new HashMap<>();
    static final Map<PlayerEntity, Map<String, Boolean>> retroTimeShiftSettings = new HashMap<>();
    static final Map<PlayerEntity, Map<Item, Integer>> objectives = new HashMap<>();
    static final Map<PlayerEntity, List<ItemStack>> startingInventory = new HashMap<>();
    static final Map<PlayerEntity, PausedTimewarpState> pausedTimewarps = new HashMap<>();

    private final Map<UUID, BlockPos[]> playerCornerSelections = new HashMap<>();
    private int nextAreaId = 0;

    @Override
    public void onInitialize() {
        instance = this;

        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "timewarp_axe"), TIMEWARP_AXE);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(new ItemStack(TIMEWARP_AXE)));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TimewarpCommands.register(dispatcher));

        LOGGER.info("[Timewarp] Are you ready to go back in time?");

        ServerLifecycleEvents.SERVER_STARTING.register(this::loadDataOnServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::saveDataOnServerStop);
        ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTicks() % SAVE_INTERVAL == 0) saveDataOnServerStop(server);
        });

        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            TimewarpNetworking.init();
        }
    }

    public static Timewarp getInstance() {
        return instance;
    }

    private void onServerTick(MinecraftServer server) {
        if (server.getTicks() % SAVE_INTERVAL == 0) {
            saveData(server);
        }

        server.getPlayerManager().getPlayerList().forEach(this::managePlayerShift);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handlePlayerAreaFeatures(player);
        }
    }

    private void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();

        if (server.isDedicated()) {
            sendTimewarpDataToClient(player);
        }
    }

    private void handlePlayerAreaFeatures(ServerPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        TimewarpArea area = getAreaContainingPosition(playerPos);

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

    private void managePlayerShift(ServerPlayerEntity player) {
        if (activePlayerAreas.containsKey(player)) {
            return;
        }

        if (retroShiftActive.getOrDefault(player, false)) {
            int shiftDuration = shiftDurationTimers.getOrDefault(player, SHIFT_DURATION_MIN);

            if (checkObjectiveCompletion(player)) {
                endTimeShift(player);
                return;
            }

            if (shiftDuration <= 0) {
                endTimeShift(player);
            } else {
                shiftDurationTimers.put(player, shiftDuration - 1);
            }
            return;
        }

        int timeUntilShift;
        if (TIME_UNTIL_SHIFT_MIN != TIME_UNTIL_SHIFT_MAX) {
            timeUntilShift = playerShiftTimers.getOrDefault(player, TIME_UNTIL_SHIFT_MIN + random.nextInt(TIME_UNTIL_SHIFT_MAX - TIME_UNTIL_SHIFT_MIN));
        } else {
            timeUntilShift = playerShiftTimers.getOrDefault(player, TIME_UNTIL_SHIFT_MIN);
        }

        if (timeUntilShift <= 0) {
            triggerAutomaticTimeShift(player);
            resetTimer(player);
        } else {
            playerShiftTimers.put(player, timeUntilShift - 1);
        }
    }

    boolean checkObjectiveCompletion(PlayerEntity player) {
        if (!objectives.containsKey(player)) return false;

        Map<Item, Integer> objective = objectives.get(player);
        Item targetItem = objective.keySet().iterator().next();
        int requiredAmount = objective.get(targetItem);

        int additionalItems = countAdditionalItems(player, targetItem);
        return additionalItems >= requiredAmount;
    }

    private int countAdditionalItems(PlayerEntity player, Item targetItem) {
        List<ItemStack> currentInventory = player.getInventory().main;
        int initialCount = startingInventory.get(player).stream()
                .filter(stack -> stack.getItem() == targetItem)
                .mapToInt(ItemStack::getCount)
                .sum();

        int currentCount = currentInventory.stream()
                .filter(stack -> stack.getItem() == targetItem)
                .mapToInt(ItemStack::getCount)
                .sum();

        return currentCount - initialCount;
    }

    public static Item getObjectiveItem(PlayerEntity player) {
        Map<Item, Integer> objective = objectives.get(player);
        return objective != null ? objective.keySet().iterator().next() : null;
    }

    public static int getObjectiveAmount(PlayerEntity player) {
        Map<Item, Integer> objective = objectives.get(player);
        return objective != null ? objective.values().iterator().next() : 0;
    }

    private void endTimeShift(ServerPlayerEntity player) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            TimewarpClient.getInstance().endTimeShift(player);
        }

        retroShiftActive.put(player, false);
        resetShiftSettings(player);
        objectives.remove(player);
        startingInventory.remove(player);
    }

    public static int getShiftRemainingTime(PlayerEntity player) {
        return shiftDurationTimers.getOrDefault(player, 0);
    }

    public static boolean isRetroShiftActive(PlayerEntity player) {
        return retroShiftActive.getOrDefault(player, false);
    }

    private void pauseTimewarp(ServerPlayerEntity player) {
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

    private void resumePausedTimewarp(ServerPlayerEntity player) {
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

    private void activateAllFeatures(ServerPlayerEntity player, TimewarpArea area) {
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

    private void deactivateAreaFeatures(ServerPlayerEntity player) {
        retroShiftActive.put(player, false);
        retroTimeShiftSettings.remove(player);
    }

    public static boolean getRetroSetting(PlayerEntity player, String setting) {
        return retroTimeShiftSettings.getOrDefault(player, new HashMap<>()).getOrDefault(setting, false);
    }

    public void triggerAutomaticTimeShift(ServerPlayerEntity player) {
        if (ENABLE_TRIGGERING) {
            TimewarpNetworking.sendTimewarpDataToClient(player);
        }
    }

    public void triggerTimeShift(ServerPlayerEntity player) {
        TimewarpNetworking.sendTimewarpDataToClient(player);
    }

    public static Item getTimewarpAxeItem() {
        return TIMEWARP_AXE;
    }

    public void handleCornerSelection(PlayerEntity player, BlockPos firstCorner, BlockPos secondCorner) {
        UUID playerId = player.getUuid();

        if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
            TimewarpClient.getInstance().handleClientCornerSelection(playerId, firstCorner, secondCorner);
        } else {
            handleServerCornerSelection(player, firstCorner, secondCorner);
        }
    }

    private void handleServerCornerSelection(PlayerEntity player, BlockPos corner1, BlockPos corner2) {
        TimewarpClientNetworking.sendCornerSelectionToServer(player, corner1, corner2);
    }

    public void storeCornerSelection(UUID playerId, BlockPos corner1, BlockPos corner2) {
        playerCornerSelections.put(playerId, new BlockPos[]{corner1, corner2});
    }

    public BlockPos[] getAndClearCornerSelection(UUID playerId) {
        return playerCornerSelections.remove(playerId);
    }


    public int addTimewarpArea(BlockPos corner1, BlockPos corner2, PlayerEntity player, String name) {
        for (TimewarpArea area : timewarpAreas) {
            if (areasOverlap(area, corner1, corner2)) return -1;
        }

        TimewarpArea newArea = new TimewarpArea(corner1, corner2, player.getUuid(), name, nextAreaId++);
        timewarpAreas.add(newArea);

        TimewarpAxe.clearSelection(player);
        return newArea.getId();
    }

    public void deleteTimewarpArea(int id) {
        timewarpAreas.removeIf(area -> area.getId() == id);
    }

    public Optional<TimewarpArea> getTimewarpAreaById(int id) {
        return timewarpAreas.stream().filter(area -> area.getId() == id).findFirst();
    }

    public List<TimewarpArea> getTimewarpAreas() {
        return Collections.unmodifiableList(timewarpAreas);
    }

    TimewarpArea getAreaContainingPosition(BlockPos pos) {
        for (TimewarpArea area : timewarpAreas) {
            if (area.contains(pos)) {
                return area;
            }
        }
        return null;
    }

    private boolean areasOverlap(TimewarpArea area, BlockPos corner1, BlockPos corner2) {
        return area.contains(corner1) || area.contains(corner2) ||
                area.contains(new BlockPos(corner1.getX(), corner1.getY(), corner2.getZ())) ||
                area.contains(new BlockPos(corner2.getX(), corner2.getY(), corner1.getZ()));
    }

    public boolean isPlayerInArea(PlayerEntity player) {
        return activePlayerAreas.containsKey(player);
    }

    public void loadDataOnServerStart(MinecraftServer server) {
        TimewarpData data = DataHandler.loadData(server);

        SHIFT_DURATION_MIN = data.preferences.shiftDurationMin != 0 ? data.preferences.shiftDurationMin : 1200;
        SHIFT_DURATION_MAX = data.preferences.shiftDurationMax != 0 ? data.preferences.shiftDurationMax : 2400;
        TIME_UNTIL_SHIFT_MIN = data.preferences.timeUntilShiftMin != 0 ? data.preferences.timeUntilShiftMin : 1200;
        TIME_UNTIL_SHIFT_MAX = data.preferences.timeUntilShiftMax != 0 ? data.preferences.timeUntilShiftMax : 2400;
        MIN_OP_LEVEL = data.preferences.opCommandLevel != 0 ? data.preferences.opCommandLevel : 2;
        SAVE_INTERVAL = data.preferences.saveInterval != 0 ? data.preferences.saveInterval : 12000;
        ENABLE_TRIGGERING = data.preferences.enableTriggering;

        timewarpAreas.clear();
        timewarpAreas.addAll(data.timewarpAreas);

        nextAreaId = data.timewarpAreas.stream()
                .mapToInt(TimewarpArea::getId)
                .max()
                .orElse(0) + 1;

        saveData(server);
    }

    private void saveDataOnServerStop(MinecraftServer server) {
        TimewarpData data = new TimewarpData();
        data.timewarpAreas = new ArrayList<>(timewarpAreas);

        data.preferences.shiftDurationMin = SHIFT_DURATION_MIN;
        data.preferences.shiftDurationMax = SHIFT_DURATION_MAX;
        data.preferences.timeUntilShiftMin = TIME_UNTIL_SHIFT_MIN;
        data.preferences.timeUntilShiftMax = TIME_UNTIL_SHIFT_MAX;
        data.preferences.saveInterval = SAVE_INTERVAL;
        data.preferences.opCommandLevel = MIN_OP_LEVEL;
        data.preferences.enableTriggering = ENABLE_TRIGGERING;

        DataHandler.saveData(server, data);
    }

    public void loadDataFromServer(TimewarpData data) {
        timewarpAreas.clear();
        timewarpAreas.addAll(data.timewarpAreas);

        SHIFT_DURATION_MIN = data.preferences.shiftDurationMin;
        SHIFT_DURATION_MAX = data.preferences.shiftDurationMax;
        TIME_UNTIL_SHIFT_MIN = data.preferences.timeUntilShiftMin;
        TIME_UNTIL_SHIFT_MAX = data.preferences.timeUntilShiftMax;
        SAVE_INTERVAL = data.preferences.saveInterval;
        MIN_OP_LEVEL = data.preferences.opCommandLevel;
        ENABLE_TRIGGERING = data.preferences.enableTriggering;
    }

    public void saveData(MinecraftServer server) {
        TimewarpData data = new TimewarpData();
        data.timewarpAreas = this.timewarpAreas;

        data.preferences.shiftDurationMin = SHIFT_DURATION_MIN;
        data.preferences.shiftDurationMax = SHIFT_DURATION_MAX;
        data.preferences.timeUntilShiftMin = TIME_UNTIL_SHIFT_MIN;
        data.preferences.timeUntilShiftMax = TIME_UNTIL_SHIFT_MAX;
        data.preferences.saveInterval = SAVE_INTERVAL;
        data.preferences.opCommandLevel = MIN_OP_LEVEL;
        data.preferences.enableTriggering = ENABLE_TRIGGERING;

        DataHandler.saveData(server, data);
    }

    public void sendTimewarpDataToClient(ServerPlayerEntity player) {
        TimewarpData data = new TimewarpData();
        data.timewarpAreas = new ArrayList<>(this.timewarpAreas);

        data.preferences.shiftDurationMin = SHIFT_DURATION_MIN;
        data.preferences.shiftDurationMax = SHIFT_DURATION_MAX;
        data.preferences.timeUntilShiftMin = TIME_UNTIL_SHIFT_MIN;
        data.preferences.timeUntilShiftMax = TIME_UNTIL_SHIFT_MAX;
        data.preferences.saveInterval = SAVE_INTERVAL;
        data.preferences.opCommandLevel = MIN_OP_LEVEL;
        data.preferences.enableTriggering = ENABLE_TRIGGERING;

        TimewarpNetworking.sendTimewarpDataToClient(player, data);
    }

    public void updatePlayerData(MinecraftServer server, UUID playerId, Map<Item, Integer> objectivesMap, Map<String, Boolean> featuresMap, int shiftDuration) {
        Optional<ServerPlayerEntity> optionalPlayer = server.getPlayerManager().getPlayerList().stream()
                .filter(player -> player.getUuid().equals(playerId))
                .findFirst();

        if (optionalPlayer.isPresent()) {
            ServerPlayerEntity player = optionalPlayer.get();

            startingInventory.put(player, new ArrayList<>(player.getInventory().main));

            objectives.put(player, objectivesMap);

            retroTimeShiftSettings.put(player, featuresMap);

            shiftDurationTimers.put(player, shiftDuration);

            retroShiftActive.put(player, true);
        } else {
            LOGGER.warn("[Timewarp] Player with UUID {} not found when updating player data.", playerId);
        }
    }

    private void resetTimer(ServerPlayerEntity player) {
        if (TIME_UNTIL_SHIFT_MIN != TIME_UNTIL_SHIFT_MAX) {
            playerShiftTimers.put(player, TIME_UNTIL_SHIFT_MIN + random.nextInt(TIME_UNTIL_SHIFT_MAX - TIME_UNTIL_SHIFT_MIN));
        } else {
            playerShiftTimers.put(player, TIME_UNTIL_SHIFT_MIN);
        }
    }

    void resetShiftSettings(PlayerEntity player) {
        retroTimeShiftSettings.remove(player);
        shiftDurationTimers.remove(player);
    }
}