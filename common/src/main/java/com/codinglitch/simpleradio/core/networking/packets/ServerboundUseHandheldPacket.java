package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ServerboundUseHandheldPacket(boolean state) implements CustomPacket {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "use_handheld_packet");
    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.state);
    }

    public static ServerboundUseHandheldPacket read(FriendlyByteBuf buffer) {
        return new ServerboundUseHandheldPacket(buffer.readBoolean());
    }
}