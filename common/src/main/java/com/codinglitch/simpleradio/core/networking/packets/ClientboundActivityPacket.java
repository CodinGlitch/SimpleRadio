package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ClientboundActivityPacket(float activity, short identifier) implements CustomPacket {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "activity");
    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.activity);
        buffer.writeShort(this.identifier);
    }

    public static ClientboundActivityPacket read(FriendlyByteBuf buffer) {
        return new ClientboundActivityPacket(buffer.readFloat(), buffer.readShort());
    }
}