package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.*;
import com.codinglitch.simpleradio.central.ConfigHolder;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.core.Listeners;
import com.codinglitch.simpleradio.core.Speakers;
import com.codinglitch.simpleradio.core.central.FrequencyChannel;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.routers.Listener;
import com.codinglitch.simpleradio.routers.Router;
import com.codinglitch.simpleradio.routers.RouterContainer;
import com.codinglitch.simpleradio.routers.Speaker;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

public class RadioManager extends ServerSimpleRadioApi {
    private static final RadioManager INSTANCE = new RadioManager();

    private static final Frequencies FREQUENCIES = new FrequenciesImpl();
    private static final Speakers SPEAKERS = new SpeakersImpl();
    private static final Listeners LISTENERS = new ListenersImpl();

    // double queue for the win
    private static final ArrayList<QueuedSource> pendingSources = new ArrayList<>();
    private static final ArrayList<QueuedSource> sourceQueue = new ArrayList<>();

    public static class QueuedSource {
        public Source source;

        public Router router;
        public int time;
        public QueuedSource(Source source, Router router, int time) {
            this.source = source;
            this.router = router;
            this.time = time;
        }

    }

    private static final Queue<Runnable> pendingModifications = new LinkedList<>();
    static final Map<Short, Router> routers = new HashMap<>();

    public static RadioManager getInstance() {
        return INSTANCE;
    }

    public RadioManager() {}

    @Override
    public Frequencies frequencies() {
        return FREQUENCIES;
    }

    @Override
    public Speakers speakers() {
        return SPEAKERS;
    }

    @Override
    public Listeners listeners() {
        return LISTENERS;
    }

    @Override
    public ConfigHolder getConfig() {
        return null; //idk yet
    }

    public <R extends Router> void putRouter(@Nullable RouterContainer<R> container, R router) {
        if (container != null) {
            container.add(router);
        } else {
            pushRouter(router);
        }
    }

    public <R extends Router> short pushRouter(R router) {
        return pushRouter(routers, router);
    }
    public <R extends Router> short pushRouter(Map<Short, R> map, R router) {
        RadioRouter radioRouter = (RadioRouter) router;
        for (short identifier = Short.MIN_VALUE; identifier < Short.MAX_VALUE; identifier++) {
            if (map.containsKey(identifier)) continue;

            radioRouter.identifier = identifier;
            map.put(identifier, (R) radioRouter);

            return identifier;
        }

        return Short.MAX_VALUE;
    }

    public short getIdentifier(Predicate<Router> filter) {
        Optional<Map.Entry<Short, Router>> result = routers.entrySet().stream().filter(entry -> filter.test(entry.getValue())).findFirst();
        return result.map(Map.Entry::getKey).orElse(Short.MAX_VALUE);
    }

    // ---- Routers ---- \\

    @Override
    public List<Router> getRouters() {
        return routers.values().stream().toList();
    }

    @Override
    public void removeRouter(Router router) {
        removeRouter(router::equals);
    }
    @Override
    public void removeRouter(Predicate<Router> criteria) {
        routers.entrySet().removeIf(entry -> criteria.test(entry.getValue()));
    }
    @Override
    public void removeRouter(short identifier) {
        routers.remove(identifier);
    }

    @Override
    public void removeRouter(UUID uuid) {
        removeRouter(router -> router.getReference().equals(uuid));
    }
    @Override
    public void removeRouter(Entity owner) {
        removeRouter(router -> router.getOwner() == owner);
    }
    @Override
    public void removeRouter(WorldlyPosition location) {
        removeRouter(router -> router.getPosition() != null && router.getPosition().equals(location));
    }

    @Override
    public Router getRouter(Predicate<Router> filter) {
        Optional<Map.Entry<Short, Router>> result = routers.entrySet().stream().filter(entry -> filter.test(entry.getValue())).findFirst();
        return result.map(Map.Entry::getValue).orElse(null);
    }
    @Override
    public Router getRouter(short identifier) {
        return routers.get(identifier);
    }

    @Override
    public Router getRouter(UUID reference, @Nullable String type) {
        return getRouter(router ->
                router.getReference().equals(reference) && (type == null ? router.getClass().equals(RadioRouter.class) : router.getClass().getSimpleName().equals(type))
        );
    }
    @Override
    public Router getRouter(UUID reference) {
        return getRouter(router -> router.getReference().equals(reference));
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
    public void registerRouter(Router router) {
        putRouter(null, router);
    }

    @Override
    public Router getRouterSided(UUID reference, boolean isClient) {
        return isClient ? ClientRadioManager.getRouter(reference) : getRouter(reference);
    }
    @Override
    public Router getRouterSided(UUID reference, @Nullable String type, boolean isClient) {
        return isClient ? ClientRadioManager.getRouter(reference, type) : getRouter(reference, type);
    }
    @Override
    public void registerRouterSided(Router router, boolean isClient, @Nullable Frequency frequency) {
        CommonSimpleRadio.debug("Adding {} of reference {}", router.getClass().getSimpleName(), router.getReference());
        if (isClient) {
            ClientRadioManager.registerRouter(router);
        } else {
            if (router instanceof RadioSpeaker speaker) {
                SPEAKERS.register(speaker);
            } else if (router instanceof RadioListener listener) {
                LISTENERS.register(listener);
            } else if (router instanceof RadioReceiver receiver) {
                if (frequency != null) frequency.registerReceiver(receiver);
            } else if (router instanceof RadioTransmitter transmitter) {
                if (frequency != null) frequency.registerTransmitter(transmitter);
            } else {
                registerRouter(router);
            }
        }
    }
    @Override
    public void removeRouterSided(UUID uuid, boolean isClient) {
        if (isClient) {
            ClientRadioManager.removeRouter(uuid);
        } else {
            removeRouter(uuid);
        }
    }
    @Override
    public void removeRouterSided(Router router, boolean isClient) {
        if (isClient) {
            ClientRadioManager.removeRouter(router);
        } else {
            removeRouter(router);
        }
    }

    // -------- \\

    public static void close() {
        FrequenciesImpl.close();

        SpeakersImpl.close();
        ListenersImpl.close();
        routers.clear();
    }

    public static <R extends Router> void validate(List<R> container) {
        container.removeIf(Predicate.not(Router::validate));
        container.removeIf(entry -> entry.getOwner() == null && entry.getPosition() == null);
    }

    public static void garbageCollect() {
        FrequenciesImpl.garbageCollect();

        SpeakersImpl.garbageCollect();
        ListenersImpl.garbageCollect();

        routers.entrySet().removeIf(entry -> !entry.getValue().validate());
        routers.entrySet().removeIf(entry -> entry.getValue().getOwner() == null && entry.getValue().getPosition() == null);
    }

    public void levelTick(ServerLevel level) {

    }

    public void serverTick(int tickCount) {
        if (tickCount % 20 == 0) {
            garbageCollect();
        }

        // -- Receiver, Transmitter and Listener ticking -- \\
        List<Frequency> frequencies = FREQUENCIES.get();
        for (Frequency frequency : frequencies) {
            ((FrequencyChannel) frequency).serverTick(tickCount);
        }

        for (Listener listener : LISTENERS.get()) {
            ((RadioListener) listener).tick(tickCount);
        }
        for (Speaker speaker : SPEAKERS.get()) {
            ((RadioSpeaker) speaker).tick(tickCount);
        }

        applyModifications();

        sourceQueue.addAll(pendingSources);
        pendingSources.clear();

        // i must be stupid
        List<QueuedSource> acceptedSources = new ArrayList<>();
        Iterator<QueuedSource> iterator = sourceQueue.iterator();
        while (iterator.hasNext()) {
            QueuedSource source = iterator.next();
            source.time--;

            if (source.time <= 0) {
                acceptedSources.add(source);
                iterator.remove();
            }
        }

        for (QueuedSource source : acceptedSources) {
            source.router.accept(source.source);
        }
    }

    private void applyModifications() {
        for (int i = 0; i < pendingModifications.size(); i++) {
            Runnable modification = pendingModifications.poll();
            if (modification == null) break;
            modification.run();
        }
    }

    public void queueSource(Source source, Router destination, int delay) {
        pendingSources.add(new QueuedSource(source, destination, delay));
    }
    public void dequeueSource(Predicate<QueuedSource> criteria) {
        pendingSources.removeIf(criteria);
        sourceQueue.removeIf(criteria);
    }

    public boolean readQueue(Predicate<QueuedSource> filter) {
        for (QueuedSource source : sourceQueue) {
            if (filter.test(source)) return true;
        }
        for (QueuedSource source : pendingSources) {
            if (filter.test(source)) return true;
        }

        return false;
    }

    @Override
    public void shortAt(WorldlyPosition location) {
        Level level = location.level;
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, location.x, location.y, location.z, SimpleRadioSounds.SHORT_CIRCUIT, SoundSource.BLOCKS, 0.3f, 0.9f + level.random.nextFloat()*0.2f);

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    location.x, location.y, location.z, 10,
                    -0.2+level.random.nextDouble()*0.4, -0.2+level.random.nextDouble()*0.4, -0.2+level.random.nextDouble()*0.4, 1
            );
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    location.x, location.y, location.z, 8,
                    -0.2+level.random.nextDouble()*0.4, -0.2+level.random.nextDouble()*0.4, -0.2+level.random.nextDouble()*0.4, 1
            );
            serverLevel.sendParticles(ParticleTypes.POOF,
                    location.x, location.y, location.z, 5,
                    0.2d, 0.2d, 0.2d, 0.1d
            );
        }
    }

    public enum CollectionResult {
        PASS,
        IGNORE,
        COLLECT
    }

    public boolean verifyLocationCollection(WorldlyPosition position, Class<?> clazz) {
        BlockPos pos = position.realLocation();

        CollectionResult result = CompatCore.verifyLocationCollection(position, clazz);
        if (result == CollectionResult.IGNORE) {
            return true;
        } else if (result == CollectionResult.COLLECT) {
            return false;
        }

        //TODO: this is causing issue in server, make sure reloading in chunks the routers get reregistered
        //if (!position.level.isLoaded(pos)) return false;

        BlockState state = position.level.getBlockState(pos);
        if (state.isAir()) {
            // Void air is used in place of unloaded chunks, so if this chunk is unloaded we will wait until it is loaded before checking the block itself
            return !position.level.isLoaded(pos);
        }

        Block block = state.getBlock();
        return clazz.isAssignableFrom(block.getClass()) || clazz.isAssignableFrom(block.asItem().getClass());
    }

    public boolean verifyEntityCollection(Entity entity, Predicate<ItemStack> itemCriteria) {
        CollectionResult result = CompatCore.verifyEntityCollection(entity, itemCriteria);
        if (result == CollectionResult.IGNORE) {
            return true;
        } else if (result == CollectionResult.COLLECT) {
            return false;
        }

        if (entity.isRemoved()) return false;

        if (entity instanceof Player player) {
            return player.getInventory().hasAnyMatching(itemCriteria);
        } else if (entity instanceof ItemEntity itemEntity) {
            return itemCriteria.test(itemEntity.getItem());
        } else {
            for (ItemStack stack : entity.getHandSlots()) {
                if (itemCriteria.test(stack)) return true;
            }
            return false;
        }
    }

    @Nullable
    public ItemStack isEntityHolding(Entity entity, Predicate<ItemStack> handCriteria) {
        for (ItemStack stack : entity.getHandSlots()) {
            if (handCriteria.test(stack)) return stack;
        }
        return null;
    }



    // --- Audio Gathering --- \\

    // I mixin here instead of using the appropriate events to access the channel as well as prevent duplicates
    public void onLocationalPacket(Level level, LocationalAudioChannel channel, byte[] data) {
        Vector3f senderPosition = new Vector3f((float) channel.getLocation().getX(), (float) channel.getLocation().getY(), (float) channel.getLocation().getZ());
        sendAudio(WorldlyPosition.of(senderPosition, level), channel.getId(), data);
    }

    public void onEntityPacket(Level level, EntityAudioChannel channel, EntitySoundPacket packet) {

    }

    public void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;

        ServerPlayer sender = (ServerPlayer) senderConnection.getPlayer().getPlayer();
        ServerLevel level = sender.serverLevel();


        Vector3f senderPosition = sender.position().toVector3f();
        sendAudio(WorldlyPosition.of(senderPosition, level), sender.getUUID(), event.getPacket().getOpusEncodedData());
    }

    public void sendSound(WorldlyPosition location, Holder<SoundEvent> soundHolder, float volume, float pitch, long seed) {
        sendSound(location, soundHolder, volume, pitch, 0, seed);
    }
    public void sendSound(WorldlyPosition location, Holder<SoundEvent> soundHolder, float volume, float pitch, float offset, long seed) {
        Level level = location.level;

        if (level.isClientSide) return;
        if (!SimpleRadioLibrary.SERVER_CONFIG.router.soundListening) return;
        if (!level.isLoaded(BlockPos.containing((Position) location))) return;

        SoundEvent sound = soundHolder.value();

        Map<Float, Listener> qualified = LISTENERS.getAt(location);
        for (Map.Entry<Float, Listener> entry : qualified.entrySet()) {
            float distance = entry.getKey();
            RadioListener listener = (RadioListener) entry.getValue();

            double falloff = CommonRadioPlugin.getFalloff(distance, listener.getRange());

            RadioSource newSource = new RadioSource(
                    listener.getReference(),
                    WorldlyPosition.of(location, level),
                    sound,
                    (float) (falloff * volume)
            );
            newSource.pitch = pitch;
            newSource.offset = offset;
            newSource.seed = seed;
            newSource.activity = (float) (Math.clamp(0, 15, Math.round((1 - (distance / listener.getRange()))*15)) * SimpleRadioLibrary.SERVER_CONFIG.router.activityRedstoneFactor);

            listener.onSource(newSource);
        }
    }

    @Override
    public void sendAudio(WorldlyPosition location, UUID sender, byte[] data) {
        Level level = location.level;
        Map<Float, Listener> qualified = LISTENERS.getAt(location);

        for (Map.Entry<Float, Listener> entry : qualified.entrySet()) {
            float distance = entry.getKey();
            RadioListener listener = (RadioListener) entry.getValue();

            double falloff = CommonRadioPlugin.getFalloff(distance, listener.range);

            Vector3f listenerPosition = null;
            if (listener.position != null) {
                listenerPosition = listener.position.position();
            } else if (listener.owner != null) {
                listenerPosition = listener.owner.position().toVector3f();
            }
            if (listenerPosition == null) continue;

            RadioSource newSource = new RadioSource(
                    sender,
                    WorldlyPosition.of(location, level),
                    data,
                    (float) falloff
            );

            // Decoding for initial reading
            OpusDecoder decoder = listener.getDecoder(sender);
            if (data == null || data.length == 0) {
                decoder.resetState();
            } else {
                short[] decoded = decoder.decode(data);
                newSource.activity = CommonRadioPlugin.analyzeActivity(decoded);
            }

            listener.onSource(newSource);
        }
    }
}
