package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundRadioUpdatePacket(String frequency, Frequency.Modulation modulation) implements CustomPacket {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "radio_update_packet");
    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.frequency);
        buffer.writeUtf(this.modulation.shorthand);
    }

    public static ServerboundRadioUpdatePacket read(FriendlyByteBuf buffer) {
        return new ServerboundRadioUpdatePacket(buffer.readUtf(), RadioManager.getInstance().frequencies().modulationOf(buffer.readUtf()));
    }
}