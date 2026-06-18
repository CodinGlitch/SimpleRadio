package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundUseHandheldPacket(boolean state) implements CustomPacket {
    public static CustomPacketPayload.Type<ServerboundUseHandheldPacket> TYPE = new CustomPacketPayload.Type<>(CommonSimpleRadio.id("use_handheld"));
    public static StreamCodec<RegistryFriendlyByteBuf, ServerboundUseHandheldPacket> STREAM_CODEC = StreamCodec.ofMember(
            ServerboundUseHandheldPacket::write, ServerboundUseHandheldPacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.state);
    }

    public static ServerboundUseHandheldPacket read(FriendlyByteBuf buffer) {
        return new ServerboundUseHandheldPacket(buffer.readBoolean());
    }
}