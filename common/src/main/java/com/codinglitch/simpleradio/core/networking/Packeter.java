package com.codinglitch.simpleradio.core.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface Packeter {
    default FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        this.encode(buffer);
        return buffer;
    }

    ResourceLocation resource();

    void encode(FriendlyByteBuf buf);
}
