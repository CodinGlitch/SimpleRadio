package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.core.EffectStream;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public record ClientboundSpeakSoundPacket(Holder<SoundEvent> sound, SoundSource source, int x, int y, int z, float volume, float pitch, float severity, long seed) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "speak_sound_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), this.sound, (byteBuf, event) -> {
            event.writeToNetwork(byteBuf);
        });
        buffer.writeEnum(this.source);
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
        buffer.writeInt(this.z);
        buffer.writeFloat(this.volume);
        buffer.writeFloat(this.pitch);
        buffer.writeFloat(this.severity);
        buffer.writeLong(this.seed);
    }

    public static ClientboundSpeakSoundPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundSpeakSoundPacket(
                buffer.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork),
                buffer.readEnum(SoundSource.class), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readLong()
        );
    }

    public static void handle(ClientboundSpeakSoundPacket packet) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            SoundManager soundManager = mc.getSoundManager();
            SoundEngine soundEngine = soundManager.soundEngine;

            Vec3 position = new Vec3(packet.x, packet.y, packet.z);

            SimpleSoundInstance instance = new SimpleSoundInstance(packet.sound.value(), packet.source,
                    packet.volume, packet.pitch,
                    RandomSource.create(packet.seed), new BlockPos(packet.x, packet.y, packet.z));
            instance.resolve(soundManager);

            Sound sound = instance.getSound();
            ResourceLocation path = sound.getPath();

            // --- Playback Setup --- \\
            CompletableFuture<ChannelAccess.ChannelHandle> completableFuture = soundEngine.channelAccess.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
            ChannelAccess.ChannelHandle channelHandle = completableFuture.join();

            float attenuatedVolume = Math.max(packet.volume, 1.0F) * (float) (sound.getAttenuationDistance());
            channelHandle.execute(channel -> {
                channel.setPitch(packet.pitch);
                channel.setVolume(packet.volume);

                if (instance.getAttenuation() == SoundInstance.Attenuation.LINEAR) {
                    channel.linearAttenuation(attenuatedVolume);
                } else {
                    channel.disableAttenuation();
                }

                channel.setSelfPosition(position);
                channel.setRelative(instance.isRelative());
            });

            // --- Audio Streaming --- \\
            EffectStream stream;
            try {
                InputStream inputStream = soundEngine.soundBuffers.resourceManager.open(path);
                stream = new EffectStream(inputStream);
            } catch (IOException e) {
                throw new CompletionException(e);
            }

            AudioEffect effect = new BaseAudioEffect();
            effect.volume = 1;
            effect.severity = packet.severity;

            stream.effect = effect;

            if (sound.shouldStream()) {
                channelHandle.execute(channel -> {
                    channel.attachBufferStream(stream);
                    channel.play();
                });
            } else {
                ByteBuffer byteBuffer;
                try {
                    byteBuffer = stream.readAll();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                SoundBuffer soundBuffer = new SoundBuffer(byteBuffer, stream.getFormat());
                channelHandle.execute(channel -> {
                    channel.attachStaticBuffer(soundBuffer);
                    channel.play();
                });
            }

            /* failure attempt
            Services.COMPAT.handleSound(data, position, packet.severity);

            OpusEncoder encoder = CommonRadioPlugin.commonApi.createEncoder();
            for (int i = 0; i < data.length; i += 960) {
                short[] frame = new short[960];
                for (int j = 0; j < frame.length && (j+i) < data.length; j++) {
                    short piece = data[j+i];
                    frame[j] = piece;
                }

                byte[] encodedData = encoder.encode(frame);

            }*/
        });
    }
}