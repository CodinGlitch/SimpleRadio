package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.UUID;

public record ClientboundSpeakSoundPacket(UUID routerID, SoundEvent sound, float volume, float pitch, float severity, float offset, long seed) implements CustomPacket {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "speak_sound_packet");
    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(routerID);
        buffer.writeId(Registry.SOUND_EVENT, this.sound);
        buffer.writeFloat(this.volume);
        buffer.writeFloat(this.pitch);
        buffer.writeFloat(this.severity);
        buffer.writeFloat(this.offset);
        buffer.writeLong(this.seed);
    }

    public static ClientboundSpeakSoundPacket read(FriendlyByteBuf buffer) {
        return new ClientboundSpeakSoundPacket(
                buffer.readUUID(), buffer.readById(Registry.SOUND_EVENT),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readLong()
        );
    }
}