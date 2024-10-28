package com.dooji.timewarp.network;

import com.dooji.timewarp.*;
import com.dooji.timewarp.data.TimewarpData;
import com.dooji.timewarp.network.payloads.*;
import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;

public class TimewarpNetworking {
    private static final Gson GSON = new Gson();

    public static void init() {
        PayloadTypeRegistry.playS2C().register(TimewarpSyncDataPayload.ID, TimewarpSyncDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TimewarpTriggerTimeShiftPayload.ID, TimewarpTriggerTimeShiftPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TimewarpSyncCornerSelectionPayload.ID, TimewarpSyncCornerSelectionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TimewarpSyncPlayerDataPayload.ID, TimewarpSyncPlayerDataPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TimewarpSyncCornerSelectionPayload.ID, (payload, context) -> {
            UUID playerId = payload.playerId();
            BlockPos corner1 = payload.corner1();
            BlockPos corner2 = payload.corner2();

            context.server().execute(() -> {
                Timewarp.getInstance().storeCornerSelection(playerId, corner1, corner2);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TimewarpSyncPlayerDataPayload.ID, (payload, context) -> {
            UUID playerId = payload.playerId();
            Map<Item, Integer> objectives = payload.objectives();
            Map<String, Boolean> features = payload.features();
            int shiftTime = payload.shiftTime();

            context.server().execute(() -> {
                ServerPlayerEntity player = context.server().getPlayerManager().getPlayer(playerId);
                if (player != null) {
                    Timewarp.getInstance().updatePlayerData(context.server(), player.getUuid(), objectives, features, shiftTime);
                }
            });
        });
    }

    public static void sendTimewarpDataToClient(ServerPlayerEntity player, TimewarpData data) {
        String jsonData = GSON.toJson(data);
        ServerPlayNetworking.send(player, new TimewarpSyncDataPayload(jsonData));
    }

    public static void sendTimewarpDataToClient(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new TimewarpTriggerTimeShiftPayload());
    }
}