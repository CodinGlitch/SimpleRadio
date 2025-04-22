package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ServerboundRequestRouterPacket(UUID reference, String type, short mapping) implements CustomPacket {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "request_router");
    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.reference);
        buffer.writeUtf(this.type);
        buffer.writeShort(this.mapping);
    }

    public static ServerboundRequestRouterPacket read(FriendlyByteBuf buffer) {
        return new ServerboundRequestRouterPacket(buffer.readUUID(), buffer.readUtf(), buffer.readShort());
    }
}