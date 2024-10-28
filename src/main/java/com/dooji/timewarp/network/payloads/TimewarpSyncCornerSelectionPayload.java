package com.dooji.timewarp.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public record TimewarpSyncCornerSelectionPayload(UUID playerId, BlockPos corner1, BlockPos corner2) implements CustomPayload {
    public static final CustomPayload.Id<TimewarpSyncCornerSelectionPayload> ID = new CustomPayload.Id<>(Identifier.of("timewarp", "corner_selection"));

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

    public static final PacketCodec<RegistryByteBuf, TimewarpSyncCornerSelectionPayload> CODEC = PacketCodec.tuple(
            UUID_CODEC, TimewarpSyncCornerSelectionPayload::playerId,
            BlockPos.PACKET_CODEC, TimewarpSyncCornerSelectionPayload::corner1,
            BlockPos.PACKET_CODEC, TimewarpSyncCornerSelectionPayload::corner2,
            TimewarpSyncCornerSelectionPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}