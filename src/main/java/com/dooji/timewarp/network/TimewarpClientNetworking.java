package com.dooji.timewarp.network;

import com.dooji.timewarp.*;
import com.dooji.timewarp.data.TimewarpData;
import com.dooji.timewarp.network.payloads.TimewarpSyncCornerSelectionPayload;
import com.dooji.timewarp.network.payloads.TimewarpSyncDataPayload;
import com.dooji.timewarp.network.payloads.TimewarpSyncPlayerDataPayload;
import com.dooji.timewarp.network.payloads.TimewarpTriggerTimeShiftPayload;
import com.google.gson.Gson;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class TimewarpClientNetworking {
    public static void init() {
        PayloadTypeRegistry.playS2C().register(TimewarpSyncDataPayload.ID, TimewarpSyncDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TimewarpTriggerTimeShiftPayload.ID, TimewarpTriggerTimeShiftPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TimewarpSyncCornerSelectionPayload.ID, TimewarpSyncCornerSelectionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TimewarpSyncPlayerDataPayload.ID, TimewarpSyncPlayerDataPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(TimewarpSyncDataPayload.ID, (payload, context) -> {
            String jsonData = payload.jsonData();
            context.client().execute(() -> {
                TimewarpData data = new Gson().fromJson(jsonData, TimewarpData.class);
                Timewarp.getInstance().loadDataFromServer(data);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TimewarpTriggerTimeShiftPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientPlayerEntity player = context.client().player;
                if (player != null) {
                    TimewarpClient.getInstance().triggerTimeShift(player);
                }
            });
        });
    }

    public static void sendCornerSelectionToServer(PlayerEntity player, BlockPos corner1, BlockPos corner2) {
        UUID playerId = player.getUuid();
        ClientPlayNetworking.send(new TimewarpSyncCornerSelectionPayload(playerId, corner1, corner2));
    }

    public static void sendData(UUID playerId, Map<Item, Integer> objectives, Map<String, Boolean> features, int shiftTime) {
        ClientPlayNetworking.send(new TimewarpSyncPlayerDataPayload(playerId, objectives, features, shiftTime));
    }
}