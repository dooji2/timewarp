package com.dooji.timewarp.commands;

import com.dooji.timewarp.world.TimewarpArea;
import com.dooji.timewarp.Timewarp;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TimewarpCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("timewarp")
                .requires(source -> source.hasPermissionLevel(Timewarp.MIN_OP_LEVEL))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    if (player != null) {
                        ItemStack axeStack = new ItemStack(Timewarp.getTimewarpAxeItem());
                        player.getInventory().insertStack(axeStack);
                        player.sendMessage(Text.translatable("message.timewarp.manipulate_time"), true);
                    }
                    return 1;
                })
                .then(CommandManager.literal("create").requires(source -> source.hasPermissionLevel(Timewarp.MIN_OP_LEVEL))
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    return createTimewarpArea(source, StringArgumentType.getString(context, "name"));
                                }))
                )
                .then(CommandManager.literal("edit").requires(source -> source.hasPermissionLevel(Timewarp.MIN_OP_LEVEL))
                        .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                .suggests((context, builder) -> {
                                    for (TimewarpArea area : Timewarp.getInstance().getTimewarpAreas()) {
                                        builder.suggest(String.valueOf(area.getId()));
                                    }
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("feature", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("allowStacking");
                                            builder.suggest("oldMinecart");
                                            builder.suggest("oldAnimalBehavior");
                                            builder.suggest("allowSprinting");
                                            builder.suggest("versionText");
                                            builder.suggest("oldGUI");
                                            builder.suggest("noFrontView");
                                            builder.suggest("noSneaking");
                                            builder.suggest("noSwimming");
                                            builder.suggest("oldCombat");
                                            builder.suggest("noTrading");
                                            return builder.buildFuture();
                                        })
                                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    ServerCommandSource source = context.getSource();
                                                    int id = IntegerArgumentType.getInteger(context, "id");
                                                    String feature = StringArgumentType.getString(context, "feature");
                                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                                    return editTimewarpArea(source, id, feature, enabled);
                                                })))))


                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                .suggests((context, builder) -> {
                                    Timewarp.getInstance().getTimewarpAreas().forEach(area -> builder.suggest(area.getId()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    var id = IntegerArgumentType.getInteger(context, "id");
                                    return deleteTimewarpArea(context.getSource(), id);
                                })))
                .then(CommandManager.literal("trigger").requires(source -> source.hasPermissionLevel(Timewarp.MIN_OP_LEVEL))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .suggests((context, builder) -> CommandSource.suggestMatching(
                                        context.getSource().getServer().getPlayerNames(), builder))
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    return triggerTimeShift(source, player);
                                })))
                .then(CommandManager.literal("config").requires(source -> source.hasPermissionLevel(Timewarp.MIN_OP_LEVEL))
                        .then(CommandManager.literal("reload").executes(context -> {
                            Timewarp.getInstance().loadDataOnServerStart(context.getSource().getServer());
                            context.getSource().sendFeedback(() -> Text.translatable("message.timewarp.config_reloaded"), true);
                            return 1;
                        }))
                        .then(CommandManager.literal("edit")
                                .then(CommandManager.argument("variable", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("shiftDurationMin").suggest("shiftDurationMax")
                                                    .suggest("timeUntilShiftMin").suggest("timeUntilShiftMax")
                                                    .suggest("saveInterval").suggest("opCommandLevel")
                                                    .suggest("enableTriggering");
                                            return builder.buildFuture();
                                        })
                                        .then(CommandManager.argument("value", StringArgumentType.word())
                                                .executes(context -> {
                                                    var variable = StringArgumentType.getString(context, "variable");
                                                    var value = StringArgumentType.getString(context, "value");
                                                    return editConfigPreference(context.getSource(), variable, value);
                                                })))))
                .then(CommandManager.literal("tp").requires(source -> source.hasPermissionLevel(Timewarp.MIN_OP_LEVEL))
                        .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                .suggests((context, builder) -> {
                                    Timewarp.getInstance().getTimewarpAreas().forEach(area -> builder.suggest(area.getId()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    var source = context.getSource();
                                    int id = IntegerArgumentType.getInteger(context, "id");
                                    return teleportToTimewarpArea(source, id);
                                }))));
    }

    private static int createTimewarpArea(ServerCommandSource source, String areaName) {
        PlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        UUID playerId = player.getUuid();
        BlockPos[] selection = Timewarp.getInstance().getAndClearCornerSelection(playerId);

        if (selection == null || selection[0] == null || selection[1] == null) {
            player.sendMessage(Text.translatable("message.timewarp.select_two_corners"), false);
            return 0;
        }

        int areaId = Timewarp.getInstance().addTimewarpArea(selection[0], selection[1], playerId, areaName);
        if (areaId >= 0) {
            player.sendMessage(Text.translatable("message.timewarp.area_created", areaId, areaName), false);

            MinecraftServer server = source.getServer();
            if (server.isDedicated()) {
                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    Timewarp.getInstance().sendTimewarpDataToClient(serverPlayer);
                }
            }

            return 1;
        } else {
            player.sendMessage(Text.translatable("message.timewarp.creation_failed_overlap"), false);
            return 0;
        }
    }

    private static int editTimewarpArea(ServerCommandSource source, int id, String feature, boolean enabled) {
        Optional<TimewarpArea> area = Timewarp.getInstance().getTimewarpAreaById(id);
        if (area.isPresent()) {
            if (area.get().getOwner().equals(Objects.requireNonNull(source.getPlayer()).getUuid()) || source.hasPermissionLevel(Timewarp.MIN_OP_LEVEL)) {
                area.get().setFeature(feature, enabled);
                source.sendFeedback(() -> Text.translatable("message.timewarp.feature_updated"), false);

                MinecraftServer server = source.getServer();
                if (server.isDedicated()) {
                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                        Timewarp.getInstance().sendTimewarpDataToClient(serverPlayer);
                    }
                }

                return 1;
            } else {
                source.sendFeedback(() -> Text.translatable("message.timewarp.permission_denied"), false);
                return 0;
            }
        } else {
            source.sendFeedback(() -> Text.translatable("message.timewarp.area_not_found"), false);
            return 0;
        }
    }

    private static int deleteTimewarpArea(ServerCommandSource source, int id) {
        Optional<TimewarpArea> area = Timewarp.getInstance().getTimewarpAreaById(id);
        if (area.isPresent()) {
            if (area.get().getOwner().equals(Objects.requireNonNull(source.getPlayer()).getUuid()) || source.hasPermissionLevel(Timewarp.MIN_OP_LEVEL)) {
                Timewarp.getInstance().deleteTimewarpArea(id);
                source.sendFeedback(() -> Text.translatable("message.timewarp.area_deleted"), false);

                MinecraftServer server = source.getServer();
                if (server.isDedicated()) {
                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                        Timewarp.getInstance().sendTimewarpDataToClient(serverPlayer);
                    }
                }

                return 1;
            } else {
                source.sendFeedback(() -> Text.translatable("message.timewarp.permission_denied"), false);
                return 0;
            }
        } else {
            source.sendFeedback(() -> Text.translatable("message.timewarp.area_not_found"), false);
            return 0;
        }
    }

    private static int triggerTimeShift(ServerCommandSource source, ServerPlayerEntity player) {
        if (!Timewarp.isRetroShiftActive(player)) {
            Timewarp.getInstance().triggerTimeShift(player);
            source.sendFeedback(() -> Text.translatable("message.timewarp.shift_triggered", player.getName().getString()), true);
            return 1;
        }
        source.sendFeedback(() -> Text.translatable("message.timewarp.shift_already_active"), false);
        return 0;
    }

    private static int editConfigPreference(ServerCommandSource source, String variable, String value) {
        try {
            switch (variable) {
                case "shiftDurationMin" -> Timewarp.SHIFT_DURATION_MIN = Integer.parseInt(value);
                case "shiftDurationMax" -> Timewarp.SHIFT_DURATION_MAX = Integer.parseInt(value);
                case "timeUntilShiftMin" -> Timewarp.TIME_UNTIL_SHIFT_MIN = Integer.parseInt(value);
                case "timeUntilShiftMax" -> Timewarp.TIME_UNTIL_SHIFT_MAX = Integer.parseInt(value);
                case "saveInterval" -> Timewarp.SAVE_INTERVAL = Integer.parseInt(value);
                case "opCommandLevel" -> Timewarp.MIN_OP_LEVEL = Integer.parseInt(value);
                case "enableTriggering" -> Timewarp.ENABLE_TRIGGERING = Boolean.parseBoolean(value);
                default -> {
                    source.sendFeedback(() -> Text.translatable("message.timewarp.invalid_config_variable"), false);
                    return 0;
                }
            }

            Timewarp.getInstance().saveData(source.getServer());
            Timewarp.getInstance().loadDataOnServerStart(source.getServer());

            MinecraftServer server = source.getServer();
            if (server.isDedicated()) {
                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    Timewarp.getInstance().sendTimewarpDataToClient(serverPlayer);
                }
            }
            source.sendFeedback(() -> Text.translatable("message.timewarp.config_updated", variable, value), true);

            return 1;
        } catch (NumberFormatException e) {
            source.sendFeedback(() -> Text.translatable("message.timewarp.invalid_value", variable), false);
            return 0;
        }
    }

    private static int teleportToTimewarpArea(ServerCommandSource source, int id) {
        Optional<TimewarpArea> areaOpt = Timewarp.getInstance().getTimewarpAreaById(id);
        PlayerEntity player = source.getPlayer();

        if (areaOpt.isEmpty()) {
            source.sendFeedback(() -> Text.translatable("message.timewarp.area_not_found"), false);
            return 0;
        }

        if (player == null) {
            source.sendFeedback(() -> Text.translatable("message.timewarp.player_not_found"), false);
            return 0;
        }

        if (Timewarp.isRetroShiftActive(player)) {
            player.sendMessage(Text.translatable("message.timewarp.shift_active_no_teleport"), true);
            return 0;
        }

        TimewarpArea area = areaOpt.get();
        BlockPos pos1 = area.getCorner1();
        BlockPos pos2 = area.getCorner2();

        BlockPos center = new BlockPos(
                (pos1.getX() + pos2.getX()) / 2,
                (pos1.getY() + pos2.getY()) / 2,
                (pos1.getZ() + pos2.getZ()) / 2
        );

        player.teleport(center.getX() + 0.5, center.getY(), center.getZ() + 0.5, false);

        player.sendMessage(Text.translatable("message.timewarp.teleported", area.getName()), true);
        return 1;
    }
}