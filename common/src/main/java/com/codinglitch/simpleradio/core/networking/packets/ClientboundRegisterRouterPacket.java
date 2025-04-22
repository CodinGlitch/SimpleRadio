package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundRegisterRouterPacket(short mapping, short identifier) implements CustomPacket {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "register_router");
    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeShort(this.mapping);
        buffer.writeShort(this.identifier);
    }

    public static ClientboundRegisterRouterPacket read(FriendlyByteBuf buffer) {
        return new ClientboundRegisterRouterPacket(buffer.readShort(), buffer.readShort());
    }
}