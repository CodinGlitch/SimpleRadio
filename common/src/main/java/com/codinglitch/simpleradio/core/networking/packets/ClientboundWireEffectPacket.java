package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundWireEffectPacket(int entityId, boolean reversed) implements CustomPacket {
    public static CustomPacketPayload.Type<ClientboundWireEffectPacket> TYPE = new CustomPacketPayload.Type<>(CommonSimpleRadio.id("wire_effect"));
    public static StreamCodec<RegistryFriendlyByteBuf, ClientboundWireEffectPacket> STREAM_CODEC = StreamCodec.ofMember(
            ClientboundWireEffectPacket::write, ClientboundWireEffectPacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeBoolean(this.reversed);
    }

    public static ClientboundWireEffectPacket read(RegistryFriendlyByteBuf buffer) {
        return new ClientboundWireEffectPacket(buffer.readVarInt(), buffer.readBoolean());
    }
}