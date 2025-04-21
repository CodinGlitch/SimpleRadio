package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundWireEffectPacket(int entityId, boolean reversed) implements CustomPacketPayload {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "wire_effect_packet");
    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeBoolean(this.reversed);
    }

    public static ClientboundWireEffectPacket read(FriendlyByteBuf buffer) {
        return new ClientboundWireEffectPacket(buffer.readVarInt(), buffer.readBoolean());
    }
}