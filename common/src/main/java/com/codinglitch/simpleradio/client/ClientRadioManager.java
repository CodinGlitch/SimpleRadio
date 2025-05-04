package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.ConfigHolder;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.core.central.ChannelHandleWrapper;
import com.codinglitch.simpleradio.client.core.central.ClientRouterWrapper;
import com.codinglitch.simpleradio.client.core.central.EffectStream;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundSpeakSoundPacket;
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
import com.codinglitch.simpleradio.routers.Router;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

public class ClientRadioManager extends ClientSimpleRadioApi {
    private static final ClientRadioManager INSTANCE = new ClientRadioManager();

    private static final Map<Short, PendingRouter<?>> pendingRouters = new HashMap<>();
    private static final Map<Short, ClientRouterWrapper> routers = new HashMap<>();

    // im, losing it

    public ClientRouterWrapper getWrapper(Predicate<ClientRouterWrapper> criteria) {
        Optional<Map.Entry<Short, ClientRouterWrapper>> result = routers.entrySet().stream().filter(entry -> criteria.test(entry.getValue())).findFirst();
        return result.map(Map.Entry::getValue).orElse(null);
    }
    public ClientRouterWrapper getWrapper(UUID uuid) {
        return getWrapper(wrapper -> uuid.equals(wrapper.router.getReference()));
    }
    public ClientRouterWrapper getWrapper(RadioRouter router) {
        return getWrapper(wrapper -> router.equals(wrapper.router));
    }

    @Override
    public ConfigHolder getConfig() {
        return null;
    }

    @Override
    public List<Router> getRouters() {
        return routers.values().stream().map(wrapper -> (Router) wrapper.router).toList();
    }

    @Override
    public Router getRouter(Predicate<Router> criteria) {
        Optional<Map.Entry<Short, ClientRouterWrapper>> result = routers.entrySet().stream().filter(entry -> criteria.test(entry.getValue().router)).findFirst();

        return result.map(Map.Entry::getValue).map(wrapper -> wrapper.router).orElse(null);
    }

    @Override
    public Router getRouter(short identifier) {
        ClientRouterWrapper wrapper = routers.get(identifier);
        return wrapper == null ? null : wrapper.router;
    }
    @Override
    public Router getRouter(UUID reference, @Nullable String type) {
        return getRouter(router ->
                router.getReference().equals(reference) && (type == null ? router.getClass().equals(RadioRouter.class) : router.getClass().getSimpleName().equals(type))
        );
    }
    @Override
    public Router getRouter(UUID reference) {
        return getRouter(router -> reference.equals(router.getReference()));
    }
    @Override
    public Router getRouter(Entity owner) {
        return getRouter(router -> owner.equals(router.getOwner()));
    }
    @Override
    public Router getRouter(WorldlyPosition location) {
        return getRouter(router -> location.equals(router.getPosition()));
    }

    @Override
    public RadioListener getListener(UUID uuid) {
        return (RadioListener) getRouter(router -> uuid.equals(router.getReference()) && router instanceof RadioListener);
    }
    @Override
    public RadioListener getListener(Entity owner) {
        return (RadioListener) getRouter(router -> owner.equals(router.getOwner()) && router instanceof RadioListener);
    }
    @Override
    public RadioListener getListener(WorldlyPosition location) {
        return (RadioListener) getRouter(router -> location.equals(router.getPosition()) && router instanceof RadioListener);
    }

    @Override
    public RadioSpeaker getSpeaker(UUID uuid) {
        return (RadioSpeaker) getRouter(router -> uuid.equals(router.getReference()) && router instanceof RadioSpeaker);
    }
    @Override
    public RadioSpeaker getSpeaker(Entity owner) {
        return (RadioSpeaker) getRouter(router -> owner.equals(router.getOwner()) && router instanceof RadioSpeaker);
    }
    @Override
    public RadioSpeaker getSpeaker(WorldlyPosition location) {
        return (RadioSpeaker) getRouter(router -> location.equals(router.getPosition()) && router instanceof RadioSpeaker);
    }

    @Override
    public RadioReceiver getReceiver(UUID uuid) {
        return (RadioReceiver) getRouter(router -> uuid.equals(router.getReference()) && router instanceof RadioReceiver);
    }
    @Override
    public RadioReceiver getReceiver(Entity owner) {
        return (RadioReceiver) getRouter(router -> owner.equals(router.getOwner()) && router instanceof RadioReceiver);
    }
    @Override
    public RadioReceiver getReceiver(WorldlyPosition location) {
        return (RadioReceiver) getRouter(router -> location.equals(router.getPosition()) && router instanceof RadioReceiver);
    }

    @Override
    public RadioTransmitter getTransmitter(UUID uuid) {
        return (RadioTransmitter) getRouter(router -> uuid.equals(router.getReference()) && router instanceof RadioTransmitter);
    }
    @Override
    public RadioTransmitter getTransmitter(Entity owner) {
        return (RadioTransmitter) getRouter(router -> owner.equals(router.getOwner()) && router instanceof RadioTransmitter);
    }
    @Override
    public RadioTransmitter getTransmitter(WorldlyPosition location) {
        return (RadioTransmitter) getRouter(router -> location.equals(router.getPosition()) && router instanceof RadioTransmitter);
    }

    @Override
    public <R extends Router> void registerRouter(R router) {
        PendingRouter<R> pendingRouter = PendingRouter.of(router);

        short mapping = Short.MAX_VALUE;
        for (short index = Short.MIN_VALUE; index < Short.MAX_VALUE; index++) {
            if (pendingRouters.containsKey(index)) continue;
            pendingRouters.put(index, pendingRouter);
            mapping = index;
            break;
        }

        pendingRouter.request(mapping);

        CommonSimpleRadio.debug("Requested identifier for {} with mapping {} and reference {}", router.getClass().getSimpleName(), mapping, router.getReference());
    }
    public void removeRouter(Predicate<Router> predicate) {
        routers.entrySet().removeIf(entry -> {
            if (predicate.test(entry.getValue().router)) {
                entry.getValue().close();
                return true;
            }

            return false;
        });
    }
    @Override
    public void removeRouter(Router router) {
        removeRouter(otherRouter -> otherRouter == router);
    }
    @Override
    public void removeRouter(UUID uuid) {
        removeRouter(router -> uuid.equals(router.getReference()));
    }
    @Override
    public void removeRouter(Entity owner) {
        removeRouter(router -> owner.equals(router.getOwner()));
    }
    @Override
    public void removeRouter(WorldlyPosition location) {
        removeRouter(router -> router.getPosition() != null && location.equals(router.getPosition()));
    }

    public static void finalizeRouter(short mapping, short identifier) {
        CommonSimpleRadio.debug("Received identifier {} for mapping {}", identifier, mapping);

        PendingRouter<?> pending = pendingRouters.remove(mapping);
        if (pending == null) {
            CommonSimpleRadio.warn("This should not happen! We could not find the router with mapping {} the server attempted to finalize with identifier {}!", mapping, identifier);
            return;
        }

        ((RadioRouter) pending.router).identifier = identifier;
        routers.put(identifier, ClientRouterWrapper.of(pending.router));
    }

    public static void garbageCollect() {
        INSTANCE.removeRouter(router -> !router.validate());
        INSTANCE.removeRouter(router -> router.getOwner() == null && router.getPosition() == null);

        pendingRouters.entrySet().removeIf(entry -> entry.getValue().router == null || !entry.getValue().router.validate());
        pendingRouters.entrySet().removeIf(entry -> entry.getValue().router == null || (entry.getValue().router.getOwner() == null && entry.getValue().router.getPosition() == null));
    }

    public static void tick(long gameTime) {
        if (gameTime % 20 == 0) {
            garbageCollect();

            // After garbage collection, we shall also re-request still missing routers
            Iterator<Map.Entry<Short, PendingRouter<?>>> iterator = pendingRouters.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Short, PendingRouter<?>> entry = iterator.next();

                short mapping = entry.getKey();
                PendingRouter<?> pending = entry.getValue();

                if (pending.request(mapping)) {
                    CommonSimpleRadio.debug("We missed a router, so re-requesting identifier for {} with mapping {} and reference {}", pending.getClass().getSimpleName(), mapping, pending.router.getReference());
                } else {
                    iterator.remove();
                }
            }
        }

        for (Map.Entry<Short, ClientRouterWrapper> wrapperEntry : routers.entrySet()) {
            ClientRouterWrapper wrapper = wrapperEntry.getValue();
            ((RadioRouter) wrapper.router).tick(0);

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

        ClientRouterWrapper wrapper = INSTANCE.getWrapper(packet.routerID());
        if (wrapper == null) return;

        Router router = wrapper.router;
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

        if (packet.sound().value().getLocation().equals(SoundEvents.EMPTY.getLocation()) && packet.volume() == 0) return;

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
        AudioFormat format = stream.getFormat();

        AudioEffect effect = new BaseAudioEffect();
        effect.volume = 1;
        effect.severity = packet.severity();

        stream.effect = effect;


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
        if (router.position != null) {
            location = new Vector3f(router.position.x, router.position.y, router.position.z);
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

    public static class PendingRouter<R extends Router> {
        public final R router;
        public int attempts = 0;

        public PendingRouter(R router) {
            this.router = router;
        }

        public boolean request(short mapping) {
            if (attempts > 5) {
                CommonSimpleRadio.warn("Attempted to request identifier for {} with mapping {} and reference {} at {} with no response after 5 tries. This could be indicative of a greater issue.", router.getClass().getSimpleName(), mapping, router.getReference(), router.getPosition());
                return false;
            }

            attempts++;
            ClientServices.NETWORKING.sendToServer(new ServerboundRequestRouterPacket(router.getReference(), router.getClass().getSimpleName(), mapping));
            return true;
        }

        public static <R extends Router> PendingRouter<R> of(R router) {
            return new PendingRouter<>(router);
        }
    }
}
