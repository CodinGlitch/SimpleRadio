package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record ServerboundRequestRouterPacket(UUID reference, String routerType, short mapping) implements CustomPacket {
    public static CustomPacketPayload.Type<ServerboundRequestRouterPacket> TYPE = new CustomPacketPayload.Type<>(CommonSimpleRadio.id("request_router"));
    public static StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestRouterPacket> STREAM_CODEC = StreamCodec.ofMember(
            ServerboundRequestRouterPacket::write, ServerboundRequestRouterPacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(this.reference);
        buffer.writeUtf(this.routerType);
        buffer.writeShort(this.mapping);
    }

    public static ServerboundRequestRouterPacket read(RegistryFriendlyByteBuf buffer) {
        return new ServerboundRequestRouterPacket(buffer.readUUID(), buffer.readUtf(), buffer.readShort());
    }
}