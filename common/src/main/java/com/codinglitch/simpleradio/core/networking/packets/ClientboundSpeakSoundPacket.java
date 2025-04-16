package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.central.Packeter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.UUID;

public record ClientboundSpeakSoundPacket(UUID routerID, Holder<SoundEvent> sound, float volume, float pitch, float severity, float offset, long seed) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "speak_sound_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(routerID);
        buffer.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), this.sound, (byteBuf, event) -> {
            event.writeToNetwork(byteBuf);
        });
        buffer.writeFloat(this.volume);
        buffer.writeFloat(this.pitch);
        buffer.writeFloat(this.severity);
        buffer.writeFloat(this.offset);
        buffer.writeLong(this.seed);
    }

    public static ClientboundSpeakSoundPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundSpeakSoundPacket(
                buffer.readUUID(), buffer.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readLong()
        );
    }

    public static void handle(ClientboundSpeakSoundPacket packet) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            ClientRadioManager.speakSound(packet);
        });
    }
}