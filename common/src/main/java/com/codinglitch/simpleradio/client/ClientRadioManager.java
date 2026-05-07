package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.Wiring;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.core.central.ChannelHandleWrapper;
import com.codinglitch.simpleradio.client.core.central.ClientRouterWrapper;
import com.codinglitch.simpleradio.client.core.central.EffectStream;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.core.SimpleRadioEvent;
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
import com.codinglitch.simpleradio.routers.*;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import oshi.util.tuples.Pair;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientRadioManager extends ClientSimpleRadioApi {
    public static final ClientRadioManager INSTANCE = new ClientRadioManager();

    private static final FrequenciesImpl FREQUENCIES = new FrequenciesImpl();

    private static final Map<Short, PendingRouter<?>> PENDING_ROUTERS = new HashMap<>();
    private static final Map<Short, ClientRouterWrapper> ROUTERS = new HashMap<>();

    public static void load() {
    }

    // im, losing it

    private boolean routerMatches(Router router, @Nullable String type) {
        return (type == null ? router.getClass().equals(RadioRouter.class) : router.getClass().getSimpleName().equals(type));
    }

    public ClientRouterWrapper getWrapper(Predicate<ClientRouterWrapper> criteria) {
        Optional<Map.Entry<Short, ClientRouterWrapper>> result = ROUTERS.entrySet().stream().filter(entry -> criteria.test(entry.getValue())).findFirst();
        return result.map(Map.Entry::getValue).orElse(null);
    }
    public ClientRouterWrapper getWrapper(UUID uuid) {
        return getWrapper(wrapper -> uuid.equals(wrapper.router.getReference()));
    }
    public ClientRouterWrapper getWrapper(RadioRouter router) {
        return getWrapper(wrapper -> router.equals(wrapper.router));
    }

    @Override
    public void info(Object object, Object... substitutions) {
        CommonSimpleRadio.info(object, substitutions);
    }
    @Override
    public void debug(Object object, Object... substitutions) {
        CommonSimpleRadio.info(object, substitutions);
    }
    @Override
    public void warn(Object object, Object... substitutions) {
        CommonSimpleRadio.info(object, substitutions);
    }
    @Override
    public void error(Object object, Object... substitutions) {
        CommonSimpleRadio.info(object, substitutions);
    }

    @Override
    public Frequencies frequencies() {
        return FREQUENCIES;
    }

    @Override
    public <E extends SimpleRadioEvent> void listen(Class<E> event, Consumer<E> listener) {
        RadioManager.getInstance().listen(event, listener);
    }

    @Override
    public <T> Optional<T> getConfig(String path) {
        return CommonSimpleRadio.getConfigFrom(SimpleRadioLibrary.CLIENT_CONFIG, path);
    }

    @Override
    public <T> void setConfig(String path, T value) {
        CommonSimpleRadio.setConfigFrom(SimpleRadioLibrary.CLIENT_CONFIG, path, value);
    }

    @Override
    public Router newRouter(UUID reference) {
        return new RadioRouter(reference);
    }
    @Override
    public Router newRouter(WorldlyPosition position) {
        return new RadioRouter(position);
    }
    @Override
    public Router newRouter(UUID reference, WorldlyPosition position) {
        return new RadioRouter(position, reference);
    }

    @Override
    public Source newSource(UUID owner, WorldlyPosition location, byte[] data, float volume) {
        return new RadioSource(owner, location, data, volume);
    }

    @Override
    public BlockPos travelExtension(BlockPos pos, LevelAccessor level) {
        return RadioManager.getInstance().travelExtension(pos, level);
    }

    @Override
    public List<Router> getRouters() {
        return ROUTERS.values().stream().map(wrapper -> wrapper.router).toList();
    }

    @Override
    public Router getRouter(Predicate<Router> criteria) {
        Optional<Map.Entry<Short, ClientRouterWrapper>> result = ROUTERS.entrySet().stream().filter(entry -> criteria.test(entry.getValue().router)).findFirst();

        return result.map(Map.Entry::getValue).map(wrapper -> wrapper.router).orElse(null);
    }

    @Override
    public Router getRouter(short identifier) {
        ClientRouterWrapper wrapper = ROUTERS.get(identifier);
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
    public <R extends Router> void registerRouter(R router, @Nullable Frequency frequency) {
        this.registerRouter(router);

        if (router instanceof RadioReceiver receiver) {
            if (frequency != null) frequency.registerReceiver(receiver);
        } else if (router instanceof RadioTransmitter transmitter) {
            if (frequency != null) frequency.registerTransmitter(transmitter);
        }
    }

    @Override
    public <R extends Router> void registerRouter(R router) {
        PendingRouter<R> pendingRouter = PendingRouter.of(router);

        short mapping = Short.MAX_VALUE;
        for (short index = Short.MIN_VALUE; index < Short.MAX_VALUE; index++) {
            if (PENDING_ROUTERS.containsKey(index)) continue;
            PENDING_ROUTERS.put(index, pendingRouter);
            mapping = index;
            break;
        }

        pendingRouter.request(mapping);

        CommonSimpleRadio.debug("Requested identifier for {} with mapping {} and reference {}", router.getClass().getSimpleName(), mapping, router.getReference());
    }

    public Router removeRouter(Predicate<Router> predicate) {
        List<Map.Entry<Short, ClientRouterWrapper>> removal = ROUTERS.entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue().router))
                .toList();

        if (removal.isEmpty()) return null;

        removal.forEach(entry -> {
            entry.getValue().close();
            entry.getValue().router.invalidate();
            ROUTERS.remove(entry.getKey());
        });

        return removal.stream().findFirst().get().getValue().router;
    }
    @Override
    public Router removeRouter(Router router) {
        return removeRouter(otherRouter -> otherRouter == router);
    }

    @Override
    public Router removeRouter(UUID reference) {
        return removeRouter(router -> reference.equals(router.getReference()));
    }
    @Override
    public Router removeRouter(UUID reference, @Nullable String type) {
        return removeRouter(router -> reference.equals(router.getReference()) && routerMatches(router, type));
    }

    @Override
    public Router removeRouter(Entity owner) {
        return removeRouter(router -> owner.equals(router.getOwner()));
    }
    @Override
    public Router removeRouter(Entity owner, @Nullable String type) {
        return removeRouter(router -> owner.equals(router.getOwner()) && routerMatches(router, type));
    }

    @Override
    public Router removeRouter(WorldlyPosition location) {
        return removeRouter(router -> location.equals(router.getPosition()));
    }
    @Override
    public Router removeRouter(WorldlyPosition location, @Nullable String type) {
        return removeRouter(router -> location.equals(router.getPosition()) && routerMatches(router, type));
    }

    @Override
    public Listener removeListener(UUID uuid) {
        return (RadioListener) removeRouter(router -> uuid.equals(router.getReference()) && router instanceof RadioListener);
    }
    @Override
    public Speaker removeSpeaker(UUID uuid) {
        return (RadioSpeaker) removeRouter(router -> uuid.equals(router.getReference()) && router instanceof RadioSpeaker);
    }
    @Override
    public Receiver removeReceiver(UUID uuid) {
        return (RadioReceiver) removeRouter(router -> uuid.equals(router.getReference()) && router instanceof RadioReceiver);
    }
    @Override
    public Transmitter removeTransmitter(UUID uuid) {
        return (RadioTransmitter) removeRouter(router -> uuid.equals(router.getReference()) && router instanceof RadioTransmitter);
    }

    public static void finalizeRouter(short mapping, short identifier) {
        CommonSimpleRadio.debug("Received identifier {} for mapping {}", identifier, mapping);

        PendingRouter<?> pending = PENDING_ROUTERS.remove(mapping);
        if (pending == null) {
            CommonSimpleRadio.warn("This should not happen! We could not find the router with mapping {} the server attempted to finalize with identifier {}!", mapping, identifier);
            return;
        }

        ((RadioRouter) pending.router).identifier = identifier;
        ROUTERS.put(identifier, ClientRouterWrapper.of(pending.router));
    }

    public static void garbageCollect() {
        INSTANCE.removeRouter(router -> !router.validate());
        INSTANCE.removeRouter(router -> router.getOwner() == null && router.getPosition() == null);

        PENDING_ROUTERS.entrySet().removeIf(entry -> entry.getValue().router == null || !entry.getValue().router.validate());
        PENDING_ROUTERS.entrySet().removeIf(entry -> entry.getValue().router == null || (entry.getValue().router.getOwner() == null && entry.getValue().router.getPosition() == null));

        FREQUENCIES.garbageCollect();
    }

    public static void tick(long gameTime) {
        if (gameTime % 20 == 0) {
            garbageCollect();

            // After garbage collection, we shall also re-request still missing routers
            Iterator<Map.Entry<Short, PendingRouter<?>>> iterator = PENDING_ROUTERS.entrySet().iterator();
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

        for (Map.Entry<Short, ClientRouterWrapper> wrapperEntry : ROUTERS.entrySet()) {
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
        FREQUENCIES.close();

        PENDING_ROUTERS.clear();
        ROUTERS.clear();
    }

    public static void speakSound(UUID routerID, String soundString, float volume, float pitch, float severity, float offset, long seed) {
        Minecraft mc = Minecraft.getInstance();

        SoundManager soundManager = mc.getSoundManager();
        SoundEngine soundEngine = soundManager.soundEngine;

        ClientRouterWrapper wrapper = INSTANCE.getWrapper(routerID);
        if (wrapper == null) return;

        Router router = wrapper.router;
        if (router == null) return;

        WorldlyPosition location = router.getLocation();
        Vec3 position = new Vec3(location.position());


        ChannelHandleWrapper existingChannelHandle = wrapper.getChannel(seed);
        if (existingChannelHandle != null) {
            if (existingChannelHandle.channelHandle.isStopped()) {
                wrapper.removeChannel(seed);
            } else {
                if (soundString.isEmpty() && volume == 0) {
                    existingChannelHandle.execute(Channel::stop);
                    if (existingChannelHandle.instance instanceof TickableSoundInstance tickable) {
                        soundEngine.tickingSounds.remove(tickable);
                    }
                    return;
                } else if (existingChannelHandle.currentSound.equals(soundString)) {
                    existingChannelHandle.effect.severity = severity;
                    existingChannelHandle.effect.volume = volume;

                    existingChannelHandle.execute(channel -> {
                        channel.setSelfPosition(position);
                    });

                    return;
                } else {
                    existingChannelHandle.execute(Channel::stop);
                }
            }
        }

        if (soundString.isEmpty() && volume == 0) return;


        Optional<ResourceLocation> soundLocation = ResourceLocation.read(soundString).result();
        SoundInstance instance;

        SoundEvent soundEvent = null;
        if (soundLocation.isPresent()) soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundLocation.get());

        if (soundEvent == null) {
            instance = ClientServices.COMPAT.makeSound(router, soundString, volume, pitch, severity, offset, seed);
            if (instance == null) return;

            //soundManager.play(instance);
            //return;
        } else {
            instance = new SimpleSoundInstance(soundEvent, SoundSource.BLOCKS,
                    volume, pitch,
                    RandomSource.create(seed), location.blockPos()
            );
            instance.resolve(soundManager);
        }

        Sound sound = instance.getSound();

        // --- Playback Setup --- \\
        CompletableFuture<ChannelAccess.ChannelHandle> completableFuture = soundEngine.channelAccess.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
        ChannelAccess.ChannelHandle channelHandle = completableFuture.join();

        float attenuatedVolume = Math.max(volume, 1.0F) * (float) (sound.getAttenuationDistance());
        channelHandle.execute(channel -> {
            channel.setPitch(pitch);
            channel.setVolume(volume);

            if (instance.getAttenuation() == SoundInstance.Attenuation.LINEAR) {
                channel.linearAttenuation(attenuatedVolume);
            } else {
                channel.disableAttenuation();
            }

            channel.setSelfPosition(position);
            channel.setRelative(instance.isRelative());
        });

        soundEngine.soundDeleteTime.put(instance, soundEngine.tickCount + 20);
        soundEngine.instanceToChannel.put(instance, channelHandle);
        soundEngine.instanceBySource.put(instance.getSource(), instance);
        if (instance instanceof TickableSoundInstance tickable) {
            soundEngine.tickingSounds.add(tickable);
        }

        // --- Audio Streaming --- \\
        CompletableFuture<AudioStream> future = ClientServices.COMPAT.makeSubstream(instance);

        AudioEffect effect = new BaseAudioEffect();
        effect.volume = 1;
        effect.severity = severity;

        if (sound.shouldStream()) {
            ChannelHandleWrapper channelWrapper = ChannelHandleWrapper.of(channelHandle);
            channelWrapper.effect = effect;
            channelWrapper.currentSound = soundString;
            channelWrapper.instance = instance;

            wrapper.addChannel(seed, channelWrapper);

            future.thenAccept(audioStream -> channelHandle.execute(channel -> {
                //channel.updateStream();

                int $$0 = AL10.alGetSourcei(channel.source, 4118);
                if ($$0 > 0) {
                    int[] $$1 = new int[$$0];
                    AL10.alSourceUnqueueBuffers(channel.source, $$1);
                    AL10.alDeleteBuffers($$1);
                }

                EffectStream stream = new EffectStream(audioStream);
                stream.effect = effect;

                AudioFormat format = audioStream.getFormat();

                if (offset != 0) {
                    int sampleOffset = (int)((offset * format.getSampleSizeInBits()) / 8.0F * (float)format.getChannels() * format.getSampleRate());
                    try {
                        stream.push(sampleOffset);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                channel.attachBufferStream(stream);
                channel.play();
            }));

        } else {
            future.thenAccept(audioStream -> channelHandle.execute(channel -> {
                EffectStream stream = new EffectStream(audioStream);
                stream.effect = effect;

                AudioFormat format = audioStream.getFormat();

                ByteBuffer byteBuffer;
                try {
                    byteBuffer = stream.readAll();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                SoundBuffer soundBuffer = new SoundBuffer(byteBuffer, format);
                channel.attachStaticBuffer(soundBuffer);
                channel.play();
            }));
        }
    }

    public static void speakSound(ClientboundSpeakSoundPacket packet) {
        speakSound(packet.routerID(), packet.sound(), packet.volume(), packet.pitch(), packet.severity(), packet.offset(), packet.seed());
    }

    public static void handleListenParticle(BlockState state, MicrophoneBlockEntity blockEntity) {
        RadioRouter mainRouter = (RadioRouter) blockEntity.getRouter();
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
        RadioRouter mainRouter = (RadioRouter) blockEntity.getRouter();
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
    private static final List<Pair<UUID, UUID>> connections = new ArrayList<>();

    public static void renderLevel(float frameTime) {
        for (Map.Entry<Short, ClientRouterWrapper> wrapperEntry : ROUTERS.entrySet()) {
            ClientRouterWrapper wrapper = wrapperEntry.getValue();
            RadioRouter router = (RadioRouter) wrapper.router;

            if (router.position == null) continue;

            router.updateRotation(ClientCompat.modifyRotation(router.position, router.rotation));
            router.updateLocation(ClientCompat.modifyPosition(router.position));
        }
    }

    public static void renderDebug(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vector3f camera) {
        connections.clear();
        for (Router router : ClientRadioManager.getInstance().getRouters()) {
            ClientRadioManager.renderRouter((RadioRouter) router, poseStack, bufferSource, camera);
        }
    }

    public static float[] getRouterColor(RadioRouter router) {
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

        return new float[] {r,g,b};
    }

    public static void drawRouterConnection(RadioRouter from, RadioRouter to, @Nullable Wiring wiring, PoseStack poseStack, VertexConsumer consumer, Vector3f camera) {
        if (from == null || to == null) return;

        if (from.reference.equals(to.reference)) return;
        if (!from.active) return;
        if (wiring != null && !from.distributes) return;

        float[] color = getRouterColor(from);
        float r = color[0];
        float g = color[1];
        float b = color[2];

        Vector3f location = from.getLocation().position();

        WorldlyPosition worldly = to.getLocation();
        if (worldly != null) {

            boolean hasOppositeConnection = false;
            connections.add(new Pair<>(from.reference, to.reference));
            for (Pair<UUID, UUID> connection : connections) {
                if (connection.getA().equals(to.reference) && connection.getB().equals(from.reference)) {
                    hasOppositeConnection = true;
                    break;
                }
            }

            PoseStack.Pose last = poseStack.last();
            Matrix4f lastPose = last.pose();

            Vector3f pos = worldly.position().sub(location, new Vector3f());
            Vector3f dir = pos.normalize(new Vector3f());

            Vector3f cameraDirection = camera.sub(location, new Vector3f()).normalize();
            Vector3f side = cameraDirection.cross(dir).normalize();

            if (hasOppositeConnection) {
                poseStack.translate(side.x*0.1f, side.y*0.1f, side.z*0.1f);
            }

            // Main line
            consumer.addVertex(lastPose, 0, 0, 0).setColor(r, g, b, 1f).setNormal(last, dir.x, dir.y, dir.z);
            consumer.addVertex(lastPose, pos.x, pos.y, pos.z).setColor(r, g, b, 1f).setNormal(last, dir.x, dir.y, dir.z);

            // Arrow
            int arrowCount = (int) Math.floor(pos.length());
            for (int i = 0; i < arrowCount; i++) {
                float factor = (0.5f+i) / arrowCount;

                Vector3f center = pos.mul(factor, new Vector3f());

                poseStack.translate(center.x, center.y, center.z);

                Vector3f arrowLine1 = dir.negate(new Vector3f()).add(side).normalize();
                consumer.addVertex(lastPose, 0, 0, 0).setColor(r, g, b, 1f).setNormal(last, arrowLine1.x, arrowLine1.y, arrowLine1.z);
                consumer.addVertex(lastPose, arrowLine1.x*0.1f, arrowLine1.y*0.1f, arrowLine1.z*0.1f).setColor(r, g, b, 1f).setNormal(last, arrowLine1.x, arrowLine1.y, arrowLine1.z);

                Vector3f arrowLine2 = dir.negate(new Vector3f()).sub(side).normalize();
                consumer.addVertex(lastPose, 0, 0, 0).setColor(r, g, b, 1f).setNormal(last, arrowLine2.x, arrowLine2.y, arrowLine2.z);
                consumer.addVertex(lastPose, arrowLine2.x*0.1f, arrowLine2.y*0.1f, arrowLine2.z*0.1f).setColor(r, g, b, 1f).setNormal(last, arrowLine2.x, arrowLine2.y, arrowLine2.z);

                poseStack.translate(-center.x, -center.y, -center.z);
            }


        }
    }

    public static void renderRouter(RadioRouter router, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vector3f camera) {
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        Vector3f location = null;
        if (router.position != null) {
            location = new Vector3f(router.position.x, router.position.y, router.position.z);
        } else if (router.owner != null) {
            location = router.owner.position().toVector3f();
        }

        if (location == null) return;

        float[] color = getRouterColor(router);
        float r = color[0];
        float g = color[1];
        float b = color[2];

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

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());

        AABB boundingBox = new AABB(
                -0.51f, -0.51f, -0.51f,
                0.51f, 0.51f, 0.51f
        );
        LevelRenderer.renderLineBox(poseStack, consumer, boundingBox, r, g, b, 0.8f);

        // Drawing wire/router connections
        for (Router otherRouter : new ArrayList<>(router.routers)) {
            drawRouterConnection(router, (RadioRouter) otherRouter, null, poseStack, consumer, camera);
        }
        for (Wiring wire : new ArrayList<>(router.wires)) {
            Router otherRouter = wire.transport(router);
            if (otherRouter == null) continue;
            drawRouterConnection(router, (RadioRouter) otherRouter, wire, poseStack, consumer, camera);
        }

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
