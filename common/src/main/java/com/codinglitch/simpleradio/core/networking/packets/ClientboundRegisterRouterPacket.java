package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundRegisterRouterPacket(short mapping, short identifier) implements CustomPacket {
    public static CustomPacketPayload.Type<ClientboundRegisterRouterPacket> TYPE = new CustomPacketPayload.Type<>(CommonSimpleRadio.id("register_router"));
    public static StreamCodec<RegistryFriendlyByteBuf, ClientboundRegisterRouterPacket> STREAM_CODEC = StreamCodec.ofMember(
            ClientboundRegisterRouterPacket::write, ClientboundRegisterRouterPacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeShort(this.mapping);
        buffer.writeShort(this.identifier);
    }

    public static ClientboundRegisterRouterPacket read(RegistryFriendlyByteBuf buffer) {
        return new ClientboundRegisterRouterPacket(buffer.readShort(), buffer.readShort());
    }
}