package com.codinglitch.simpleradio.core.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacket {
    default FriendlyByteBuf writeNew() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        this.write(buf);
        return buf;
    }
    void write(FriendlyByteBuf buf);

    ResourceLocation id();
}
