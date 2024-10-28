package com.dooji.timewarp.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TimewarpSyncDataPayload(String jsonData) implements CustomPayload {
    public static final CustomPayload.Id<TimewarpSyncDataPayload> ID = new CustomPayload.Id<>(Identifier.of("timewarp", "sync_data"));

    public static final PacketCodec<RegistryByteBuf, TimewarpSyncDataPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            TimewarpSyncDataPayload::jsonData,
            TimewarpSyncDataPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}