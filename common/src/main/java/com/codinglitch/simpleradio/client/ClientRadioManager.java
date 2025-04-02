package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.core.central.ChannelHandleWrapper;
import com.codinglitch.simpleradio.client.core.central.ClientRouterWrapper;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.core.central.EffectStream;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundSpeakSoundPacket;
import com.codinglitch.simpleradio.core.networking.packets.ServerboundRadioUpdatePacket;
import com.codinglitch.simpleradio.core.networking.packets.ServerboundRequestRouterPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioParticles;
import com.codinglitch.simpleradio.core.registry.blocks.MicrophoneBlock;
import com.codinglitch.simpleradio.core.registry.blocks.MicrophoneBlockEntity;
import com.codinglitch.simpleradio.core.registry.blocks.SpeakerBlock;
import com.codinglitch.simpleradio.core.registry.blocks.SpeakerBlockEntity;
import com.codinglitch.simpleradio.platform.ClientServices;
import com.codinglitch.simpleradio.radio.*;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
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
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

public class ClientRadioManager {
    // do NOT FORGET THIS
    // pending routers are added when initially registered
    // packet will lookup a pending router by reference
    // it will then assign the given identifier before registering it
    // but what about identical reference?
    // i dont know bruh
    private static final Map<Short, RadioRouter> pendingRouters = new HashMap<>();
    private static final Map<Short, ClientRouterWrapper> routers = new HashMap<>();

    public static List<RadioRouter> getRouters() {
        return routers.values().stream().map(wrapper -> wrapper.router).toList();
    }

    public static RadioRouter getRouter(Predicate<RadioRouter> criteria) {
        Optional<Map.Entry<Short, ClientRouterWrapper>> result = routers.entrySet().stream().filter(entry -> criteria.test(entry.getValue().router)).findFirst();

        return result.map(Map.Entry::getValue).map(wrapper -> wrapper.router).orElse(null);
    }

    // im, losing it

    public static ClientRouterWrapper getWrapper(Predicate<ClientRouterWrapper> criteria) {
        Optional<Map.Entry<Short, ClientRouterWrapper>> result = routers.entrySet().stream().filter(entry -> criteria.test(entry.getValue())).findFirst();
        return result.map(Map.Entry::getValue).orElse(null);
    }
    public static ClientRouterWrapper getWrapper(UUID uuid) {
        return getWrapper(wrapper -> uuid.equals(wrapper.router.reference));
    }
    public static ClientRouterWrapper getWrapper(RadioRouter router) {
        return getWrapper(wrapper -> router.equals(wrapper.router));
    }

    public static RadioRouter getRouter(short identifier) {
        ClientRouterWrapper wrapper = routers.get(identifier);
        return wrapper == null ? null : wrapper.router;
    }
    public static RadioRouter getRouter(UUID reference, @Nullable String type) {
        return getRouter(router ->
                router.reference.equals(reference) && (type == null ? router.getClass().equals(RadioRouter.class) : router.getClass().getSimpleName().equals(type))
        );
    }
    public static RadioRouter getRouter(UUID reference) {
        return ClientRadioManager.getRouter(router -> reference.equals(router.reference));
    }
    public static RadioRouter getRouter(Entity owner) {
        return ClientRadioManager.getRouter(router -> owner.equals(router.owner));
    }
    public static RadioRouter getRouter(WorldlyPosition location) {
        return ClientRadioManager.getRouter(router -> location.equals(router.location));
    }

    public static RadioListener getListener(UUID uuid) {
        return (RadioListener) ClientRadioManager.getRouter(router -> uuid.equals(router.reference) && router instanceof RadioListener);
    }
    public static RadioListener getListener(Entity owner) {
        return (RadioListener) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioListener);
    }
    public static RadioListener getListener(WorldlyPosition location) {
        return (RadioListener) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioListener);
    }

    public static RadioSpeaker getSpeaker(UUID uuid) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> uuid.equals(router.reference) && router instanceof RadioSpeaker);
    }
    public static RadioSpeaker getSpeaker(Entity owner) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioSpeaker);
    }
    public static RadioSpeaker getSpeaker(WorldlyPosition location) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioSpeaker);
    }

    public static RadioReceiver getReceiver(UUID uuid) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> uuid.equals(router.reference) && router instanceof RadioReceiver);
    }
    public static RadioReceiver getReceiver(Entity owner) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioReceiver);
    }
    public static RadioReceiver getReceiver(WorldlyPosition location) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioReceiver);
    }

    public static RadioTransmitter getTransmitter(UUID uuid) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> uuid.equals(router.reference) && router instanceof RadioTransmitter);
    }
    public static RadioTransmitter getTransmitter(Entity owner) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioTransmitter);
    }
    public static RadioTransmitter getTransmitter(WorldlyPosition location) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioTransmitter);
    }

    public static void finalizeRouter(short mapping, short identifier) {
        CommonSimpleRadio.debug("Received identifier {} for mapping {}", identifier, mapping);

        RadioRouter router = pendingRouters.remove(mapping);
        if (router == null) {
            CommonSimpleRadio.warn("This should not happen! We could not find the router with mapping {} the server attempted to finalize with identifier {}!", mapping, identifier);
            return;
        }

        router.identifier = identifier;
        routers.put(identifier, ClientRouterWrapper.of(router));
    }

    public static void registerRouter(RadioRouter router) {
        short mapping = RadioManager.pushRouter(pendingRouters, router);
        ClientServices.NETWORKING.sendToServer(new ServerboundRequestRouterPacket(router.getReference(), router.getClass().getSimpleName(), mapping));
        CommonSimpleRadio.debug("Requested identifier for {} with mapping {} and reference {}", router.getClass().getSimpleName(), mapping, router.getReference());
    }
    public static void removeRouter(Predicate<ClientRouterWrapper> predicate) {
        routers.entrySet().removeIf(entry -> {
            if (predicate.test(entry.getValue())) {
                entry.getValue().close();
                return true;
            }

            return false;
        });
    }
    public static void removeRouter(RadioRouter router) {
        removeRouter(wrapper -> wrapper.router == router);
    }
    public static void removeRouter(UUID uuid) {
        removeRouter(wrapper -> uuid.equals(wrapper.router.reference));
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

        for (Map.Entry<Short, ClientRouterWrapper> wrapperEntry : routers.entrySet()) {
            ClientRouterWrapper wrapper = wrapperEntry.getValue();
            wrapper.router.tick(0);

            for (Map.Entry<Long, ChannelHandleWrapper> entry : wrapper.audioChannels.entrySet()) {
                entry.getValue().execute(channel -> {
                    channel.setSelfPosition(new Vec3(wrapper.router.getLocation().position()));
                });
            }
        }
    }

    public static void close() {
        pendingRouters.clear();
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

    public static void handleListenParticle(BlockState state, MicrophoneBlockEntity blockEntity) {
        RadioRouter mainRouter = blockEntity.getRouter();
        if (mainRouter == null) return;

        float rotation = RotationSegment.convertToDegrees(state.getValue(MicrophoneBlock.ROTATION));

        Vector3f direction = new Vector3f(0f, 1f, 0f);
        direction.rotateX(blockEntity.currentTilt);
        direction.rotateY(Math.toRadians(-rotation));

        WorldlyPosition position = mainRouter.getLocation();

        if (mainRouter.rotation != null) {
            mainRouter.rotation.transform(direction);
        }

        Vector3f pos = position.add(direction.x*0.4f, direction.y*0.4f, direction.z*0.4f, new Vector3f());
        blockEntity.getLevel().addParticle(SimpleRadioParticles.LISTEN, pos.x, pos.y, pos.z, direction.x*0.01f, direction.y*0.01f, direction.z*0.01f);

    }

    public static void handleSpeakParticle(BlockState state, SpeakerBlockEntity blockEntity) {
        RadioRouter mainRouter = blockEntity.getRouter();
        if (mainRouter == null) return;

        Direction direction = state.getValue(SpeakerBlock.FACING);

        Vec3i dir = direction.getNormal();
        Vector3f transformedDir = new Vector3f(dir.getX(), dir.getY(), dir.getZ());

        WorldlyPosition position = mainRouter.getLocation();
        Vec3 blockPosition = blockEntity.getBlockPos().getCenter();

        if (mainRouter.rotation != null) {
            mainRouter.rotation.transform(transformedDir);
        }

        Entity camera = Minecraft.getInstance().cameraEntity;
        if (camera == null) return;

        float dot = transformedDir.normalize().dot(camera.position().toVector3f().sub(position).normalize());
        if (Math.abs(dot) > 0.65f) {
            Vec3 pos = blockPosition.relative(direction, 0.55d);
            blockEntity.getLevel().addParticle(SimpleRadioParticles.SPEAK_RING, pos.x, pos.y, pos.z, dir.getX()*0.01f, dir.getY()*0.01f, dir.getZ()*0.01f);
        } else {
            Vec3 pos = blockPosition.relative(direction, 0.9d);
            blockEntity.getLevel().addParticle(SimpleRadioParticles.SPEAK_LINE, pos.x, pos.y, pos.z, dir.getX()*0.01f, dir.getY()*0.01f, dir.getZ()*0.01f);
        }
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

    public static void onSoundEvent(ClientReceiveSoundEvent receiveSoundEvent) {

    }
}
