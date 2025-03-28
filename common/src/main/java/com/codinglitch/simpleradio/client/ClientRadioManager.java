package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.client.core.central.ChannelHandleWrapper;
import com.codinglitch.simpleradio.client.core.central.ClientRouterWrapper;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.core.central.EffectStream;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundSpeakSoundPacket;
import com.codinglitch.simpleradio.radio.*;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector3f;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

public class ClientRadioManager {
    private static final List<ClientRouterWrapper> routers = new ArrayList<>();

    public static List<RadioRouter> getRouters() {
        return routers.stream().map(wrapper -> wrapper.router).toList();
    }

    public static RadioRouter getRouter(Predicate<RadioRouter> criteria) {
        ClientRouterWrapper routerWrapper = routers.stream().filter(wrapper -> criteria.test(wrapper.router)).findFirst().orElse(null);
        if (routerWrapper == null) return null;

        return routerWrapper.router;
    }

    // im, losing it

    public static ClientRouterWrapper getWrapper(UUID uuid) {
        return routers.stream().filter(wrapper -> wrapper.router.id.equals(uuid)).findFirst().orElse(null);
    }
    public static ClientRouterWrapper getWrapper(RadioRouter router) {
        return routers.stream().filter(wrapper -> wrapper.router.equals(router)).findFirst().orElse(null);
    }

    public static RadioRouter getRouter(UUID uuid) {
        return ClientRadioManager.getRouter(router -> uuid.equals(router.id));
    }
    public static RadioRouter getRouter(Entity owner) {
        return ClientRadioManager.getRouter(router -> owner.equals(router.owner));
    }
    public static RadioRouter getRouter(WorldlyPosition location) {
        return ClientRadioManager.getRouter(router -> location.equals(router.location));
    }

    public static RadioListener getListener(UUID uuid) {
        return (RadioListener) ClientRadioManager.getRouter(router -> uuid.equals(router.id) && router instanceof RadioListener);
    }
    public static RadioListener getListener(Entity owner) {
        return (RadioListener) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioListener);
    }
    public static RadioListener getListener(WorldlyPosition location) {
        return (RadioListener) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioListener);
    }

    public static RadioSpeaker getSpeaker(UUID uuid) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> uuid.equals(router.id) && router instanceof RadioSpeaker);
    }
    public static RadioSpeaker getSpeaker(Entity owner) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioSpeaker);
    }
    public static RadioSpeaker getSpeaker(WorldlyPosition location) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioSpeaker);
    }

    public static RadioReceiver getReceiver(UUID uuid) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> uuid.equals(router.id) && router instanceof RadioReceiver);
    }
    public static RadioReceiver getReceiver(Entity owner) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioReceiver);
    }
    public static RadioReceiver getReceiver(WorldlyPosition location) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioReceiver);
    }

    public static RadioTransmitter getTransmitter(UUID uuid) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> uuid.equals(router.id) && router instanceof RadioTransmitter);
    }
    public static RadioTransmitter getTransmitter(Entity owner) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioTransmitter);
    }
    public static RadioTransmitter getTransmitter(WorldlyPosition location) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioTransmitter);
    }

    public static void registerRouter(RadioRouter router) {
        routers.add(ClientRouterWrapper.of(router));
    }
    public static void removeRouter(Predicate<ClientRouterWrapper> predicate) {
        routers.removeIf(wrapper -> {
            if (predicate.test(wrapper)) {
                wrapper.close();
                return true;
            }

            return false;
        });
    }
    public static void removeRouter(RadioRouter router) {
        removeRouter(wrapper -> wrapper.router == router);
    }
    public static void removeRouter(UUID uuid) {
        removeRouter(wrapper -> uuid.equals(wrapper.router.id));
    }
    public static void removeRouter(Entity owner) {
        removeRouter(wrapper -> owner.equals(wrapper.router.owner));
    }
    public static void removeRouter(WorldlyPosition location) {
        removeRouter(wrapper -> wrapper.router.location != null && location.equals(wrapper.router.location));
    }

    public static void garbageCollect() {
        removeRouter(wrapper -> !wrapper.router.validate());
        removeRouter(wrapper -> wrapper.router.owner == null && wrapper.router.location == null);
    }

    public static void tick(long gameTime) {
        if (gameTime % 20 == 0) {
            garbageCollect();
        }

        for (ClientRouterWrapper router : routers) {
            router.router.tick(0);

            for (Map.Entry<Long, ChannelHandleWrapper> entry : router.audioChannels.entrySet()) {
                entry.getValue().execute(channel -> {
                    channel.setSelfPosition(new Vec3(router.router.getLocation().position()));
                });
            }
        }
    }

    public static void close() {
        routers.clear();
    }

    public static void speakSound(ClientboundSpeakSoundPacket packet) {
        Minecraft mc = Minecraft.getInstance();

        SoundManager soundManager = mc.getSoundManager();
        SoundEngine soundEngine = soundManager.soundEngine;

        ClientRouterWrapper wrapper = ClientRadioManager.getWrapper(packet.routerID());
        if (wrapper == null) return;

        RadioRouter router = wrapper.router;
        if (router == null) return;

        WorldlyPosition location = router.getLocation();
        Vec3 position = new Vec3(location.position());

        ChannelHandleWrapper existingChannelHandle = wrapper.getChannel(packet.seed());
        if (existingChannelHandle != null) {
            if (existingChannelHandle.channelHandle.isStopped()) {
                wrapper.removeChannel(packet.seed());
            } else {
                if (packet.sound().value().getLocation().equals(SoundEvents.EMPTY.getLocation()) && packet.volume() == 0) {
                    existingChannelHandle.execute(Channel::stop);
                } else {
                    //if (true) return;

                    existingChannelHandle.effect.severity = packet.severity();
                    existingChannelHandle.effect.volume = packet.volume();

                    existingChannelHandle.execute(channel -> {
                        channel.setSelfPosition(position);
                    });
                }

                return;
            }
        }

        SimpleSoundInstance instance = new SimpleSoundInstance(packet.sound().value(), SoundSource.BLOCKS,
                packet.volume(), packet.pitch(),
                RandomSource.create(packet.seed()), location.blockPos());
        instance.resolve(soundManager);

        Sound sound = instance.getSound();
        ResourceLocation path = sound.getPath();

        // --- Playback Setup --- \\
        CompletableFuture<ChannelAccess.ChannelHandle> completableFuture = soundEngine.channelAccess.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
        ChannelAccess.ChannelHandle channelHandle = completableFuture.join();

        float attenuatedVolume = Math.max(packet.volume(), 1.0F) * (float) (sound.getAttenuationDistance());
        channelHandle.execute(channel -> {
            channel.setPitch(packet.pitch());
            channel.setVolume(packet.volume());

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
        effect.severity = packet.severity();

        stream.effect = effect;

        AudioFormat format = stream.getFormat();
        if (sound.shouldStream()) {
            ChannelHandleWrapper channelWrapper = ChannelHandleWrapper.of(channelHandle);
            channelWrapper.effect = effect;

            wrapper.addChannel(packet.seed(), channelWrapper);

            channelHandle.execute(channel -> {

                if (packet.offset() != 0) {
                    int sampleOffset = (int)((packet.offset() * format.getSampleSizeInBits()) / 8.0F * (float)format.getChannels() * format.getSampleRate());
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
    }

    //

    public static void renderRouter(RadioRouter router, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vector3f camera) {
        poseStack.pushPose();

        Vector3f location = null;
        if (router.location != null) {
            location = new Vector3f(router.location.x, router.location.y, router.location.z);
        } else if (router.owner != null) {
            location = router.owner.position().toVector3f();
        }

        if (location == null) return;

        float r = 0.1f;
        float g = 0.1f;
        float b = 0.1f;

        if (router instanceof RadioListener) {
            r = 1f;
            g = 1f;
        } else if (router instanceof RadioSpeaker) {
            r = 1f;
            b = 1f;
        } else if (router instanceof RadioReceiver) {
            r = 1f;
        } else if (router instanceof RadioTransmitter) {
            b = 1f;
        } else {
            g = 1f;
        }

        location = location.sub(camera);
        poseStack.translate(location.x, location.y, location.z);

        if (router.rotation != null) {
            poseStack.mulPose(router.rotation);
        }

        Vector3f newOffset;
        if (router.connectionOffset == Vec3.ZERO) {
            newOffset = new Vector3f();
        } else {
            newOffset = router.connectionOffset.toVector3f();
        }

        //Vec3 newLocation = location.getCenter().add(new Vec3(newOffset));
        AABB pointBox = new AABB(
                -0.05f, -0.05f, -0.05f,
                0.05f, 0.05f, 0.05f
        ).move(newOffset.x, newOffset.y, newOffset.z);
        DebugRenderer.renderFilledBox(poseStack, bufferSource, pointBox, r, g, b, 0.8f);

        AABB boundingBox = new AABB(
                -0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, 0.5f
        );
        LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), boundingBox, r, g, b, 0.8f);

        poseStack.popPose();
    }
}
