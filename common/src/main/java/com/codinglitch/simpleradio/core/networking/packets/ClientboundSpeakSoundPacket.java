package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.client.central.ChannelHandleWrapper;
import com.codinglitch.simpleradio.client.central.ClientRouterWrapper;
import com.codinglitch.simpleradio.client.central.EffectStream;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.radio.RadioRouter;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
            SoundManager soundManager = mc.getSoundManager();
            SoundEngine soundEngine = soundManager.soundEngine;

            ClientRouterWrapper wrapper = ClientRadioManager.getWrapper(packet.routerID);
            if (wrapper == null) return;

            RadioRouter router = wrapper.router;
            if (router == null) return;

            WorldlyPosition location = router.getLocation();
            Vec3 position = new Vec3(location.position());

            ChannelHandleWrapper existingChannelHandle = wrapper.getChannel(packet.seed);
            if (existingChannelHandle != null) {
                if (existingChannelHandle.channelHandle.isStopped()) {
                    wrapper.removeChannel(packet.seed);
                } else {
                    if (packet.sound.value().getLocation().equals(SoundEvents.EMPTY.getLocation()) && packet.volume == 0) {
                        existingChannelHandle.execute(Channel::stop);
                    } else {
                        if (true) return;

                        existingChannelHandle.effect.severity = packet.severity;
                        existingChannelHandle.effect.volume = packet.volume;

                        existingChannelHandle.execute(channel -> {
                            channel.setSelfPosition(position);
                        });
                    }

                    return;
                }
            }

            SimpleSoundInstance instance = new SimpleSoundInstance(packet.sound.value(), SoundSource.BLOCKS,
                    packet.volume, packet.pitch,
                    RandomSource.create(packet.seed), location.blockPos());
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

            AudioFormat format = stream.getFormat();
            if (sound.shouldStream()) {
                ChannelHandleWrapper channelWrapper = ChannelHandleWrapper.of(channelHandle);
                channelWrapper.effect = effect;

                wrapper.addChannel(packet.seed, channelWrapper);

                channelHandle.execute(channel -> {

                    if (packet.offset != 0) {
                        int sampleOffset = (int)((packet.offset * format.getSampleSizeInBits()) / 8.0F * (float)format.getChannels() * format.getSampleRate());
                        try {
                            stream.push(sampleOffset);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    channel.attachBufferStream(stream);

                    //AL10.alSourcei(channel.source, EXTOffset.AL_SAMPLE_OFFSET, 160000);

                    channel.play();
                });
            } else {
                ByteBuffer byteBuffer;
                try {
                    byteBuffer = stream.readAll();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                SoundBuffer soundBuffer = new SoundBuffer(byteBuffer, format);
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