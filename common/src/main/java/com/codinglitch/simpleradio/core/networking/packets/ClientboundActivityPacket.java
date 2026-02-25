package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundActivityPacket(float activity, short identifier) implements CustomPacket {
    public static CustomPacketPayload.Type<ClientboundActivityPacket> TYPE = new CustomPacketPayload.Type<>(CommonSimpleRadio.id("activity"));
    public static StreamCodec<RegistryFriendlyByteBuf, ClientboundActivityPacket> STREAM_CODEC = StreamCodec.ofMember(
            ClientboundActivityPacket::write, ClientboundActivityPacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(this.activity);
        buffer.writeShort(this.identifier);
    }

    public static ClientboundActivityPacket read(RegistryFriendlyByteBuf buffer) {
        return new ClientboundActivityPacket(buffer.readFloat(), buffer.readShort());
    }
}