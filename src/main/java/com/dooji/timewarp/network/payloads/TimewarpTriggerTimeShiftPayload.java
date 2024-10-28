package com.dooji.timewarp.network.payloads;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TimewarpTriggerTimeShiftPayload() implements CustomPayload {
    public static final CustomPayload.Id<TimewarpTriggerTimeShiftPayload> ID = new CustomPayload.Id<>(Identifier.of("timewarp", "trigger_time_shift"));

    public static final PacketCodec<RegistryByteBuf, TimewarpTriggerTimeShiftPayload> CODEC = PacketCodec.unit(new TimewarpTriggerTimeShiftPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
