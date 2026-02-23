package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundRadioUpdatePacket(String frequency, Frequency.Modulation modulation) implements CustomPacket {
    public static CustomPacketPayload.Type<ServerboundRadioUpdatePacket> TYPE = new CustomPacketPayload.Type<>(CommonSimpleRadio.id("radio_update"));
    public static StreamCodec<FriendlyByteBuf, ServerboundRadioUpdatePacket> STREAM_CODEC = StreamCodec.ofMember(
            ServerboundRadioUpdatePacket::write, ServerboundRadioUpdatePacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.frequency);
        buffer.writeUtf(this.modulation.shorthand);
    }

    public static ServerboundRadioUpdatePacket read(FriendlyByteBuf buffer) {
        return new ServerboundRadioUpdatePacket(buffer.readUtf(), RadioManager.getInstance().frequencies().modulationOf(buffer.readUtf()));
    }
}