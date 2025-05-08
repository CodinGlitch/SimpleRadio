package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;

import java.util.UUID;

public record ClientboundSpeakSoundPacket(UUID routerID, Holder<SoundEvent> sound, float volume, float pitch, float severity, float offset, long seed) implements CustomPacket {
    public static CustomPacketPayload.Type<ClientboundSpeakSoundPacket> TYPE = new CustomPacketPayload.Type<>(CommonSimpleRadio.id("speak_sound"));
    public static StreamCodec<RegistryFriendlyByteBuf, ClientboundSpeakSoundPacket> STREAM_CODEC = StreamCodec.ofMember(
            ClientboundSpeakSoundPacket::write, ClientboundSpeakSoundPacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(routerID);
        SoundEvent.STREAM_CODEC.encode(buffer, this.sound);
        buffer.writeFloat(this.volume);
        buffer.writeFloat(this.pitch);
        buffer.writeFloat(this.severity);
        buffer.writeFloat(this.offset);
        buffer.writeLong(this.seed);
    }

    public static ClientboundSpeakSoundPacket read(RegistryFriendlyByteBuf buffer) {
        return new ClientboundSpeakSoundPacket(
                buffer.readUUID(), SoundEvent.STREAM_CODEC.decode(buffer),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readLong()
        );
    }
}