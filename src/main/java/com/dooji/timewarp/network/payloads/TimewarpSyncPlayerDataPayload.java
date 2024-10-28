package com.dooji.timewarp.network.payloads;

import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record TimewarpSyncPlayerDataPayload(UUID playerId, Map<Item, Integer> objectives, Map<String, Boolean> features, int shiftTime) implements CustomPayload {
    public static final CustomPayload.Id<TimewarpSyncPlayerDataPayload> ID = new CustomPayload.Id<>(Identifier.of("timewarp", "player_data"));

    public static final PacketCodec<RegistryByteBuf, Long> LONG_CODEC = new PacketCodec<>() {
        @Override
        public Long decode(RegistryByteBuf buf) {
            return buf.readLong();
        }

        @Override
        public void encode(RegistryByteBuf buf, Long value) {
            buf.writeLong(value);
        }
    };

    public static final PacketCodec<RegistryByteBuf, UUID> UUID_CODEC = PacketCodec.tuple(
            LONG_CODEC, UUID::getMostSignificantBits,
            LONG_CODEC, UUID::getLeastSignificantBits,
            (most, least) -> new UUID(most, least)
    );

    public static final PacketCodec<RegistryByteBuf, Integer> INTEGER_CODEC = new PacketCodec<>() {
        @Override
        public Integer decode(RegistryByteBuf buf) {
            return buf.readInt();
        }

        @Override
        public void encode(RegistryByteBuf buf, Integer value) {
            buf.writeInt(value);
        }
    };

    public static PacketCodec<RegistryByteBuf, Map<Item, Integer>> createObjectivesCodec() {
        return new PacketCodec<>() {
            @Override
            public Map<Item, Integer> decode(RegistryByteBuf buf) {
                int size = buf.readVarInt();
                Map<Item, Integer> objectives = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    String itemId = buf.readString();
                    Item item = Registries.ITEM.get(Identifier.of(itemId));
                    Integer count = buf.readVarInt();
                    objectives.put(item, count);
                }
                return objectives;
            }

            @Override
            public void encode(RegistryByteBuf buf, Map<Item, Integer> objectives) {
                buf.writeVarInt(objectives.size());
                for (Map.Entry<Item, Integer> entry : objectives.entrySet()) {
                    String itemId = Registries.ITEM.getId(entry.getKey()).toString();
                    buf.writeString(itemId);
                    buf.writeVarInt(entry.getValue());
                }
            }
        };
    }

    public static PacketCodec<RegistryByteBuf, Map<String, Boolean>> createFeaturesCodec() {
        return new PacketCodec<>() {
            @Override
            public Map<String, Boolean> decode(RegistryByteBuf buf) {
                int size = buf.readVarInt();
                Map<String, Boolean> features = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    String key = buf.readString();
                    Boolean value = buf.readBoolean();
                    features.put(key, value);
                }
                return features;
            }

            @Override
            public void encode(RegistryByteBuf buf, Map<String, Boolean> features) {
                buf.writeVarInt(features.size());
                for (Map.Entry<String, Boolean> entry : features.entrySet()) {
                    buf.writeString(entry.getKey());
                    buf.writeBoolean(entry.getValue());
                }
            }
        };
    }

    public static final PacketCodec<RegistryByteBuf, TimewarpSyncPlayerDataPayload> CODEC = PacketCodec.tuple(
            UUID_CODEC, TimewarpSyncPlayerDataPayload::playerId,
            createObjectivesCodec(), TimewarpSyncPlayerDataPayload::objectives,
            createFeaturesCodec(), TimewarpSyncPlayerDataPayload::features,
            INTEGER_CODEC, TimewarpSyncPlayerDataPayload::shiftTime,
            TimewarpSyncPlayerDataPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}