package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.api.SimpleRadioApi;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class RadioManager implements SimpleRadioApi {
    private static RadioManager INSTANCE;

    // double queue for the win
    private static final ArrayList<QueuedSource> pendingSources = new ArrayList<>();
    private static final ArrayList<QueuedSource> sourceQueue = new ArrayList<>();
    public static class QueuedSource {
        public RadioSource source;

        public RadioRouter router;
        public int time;
        public QueuedSource(RadioSource source, RadioRouter router, int time) {
            this.source = source;
            this.router = router;
            this.time = time;
        }

    }

    private static final Map<UUID, Vector3f> playerVelocities = new HashMap<>();

    private static final Queue<Runnable> pendingModifications = new LinkedList<>();
    private static final RouterContainer<RadioSpeaker> speakers = new RouterContainer<>();
    private static final RouterContainer<RadioListener> listeners = new RouterContainer<>();
    static final Map<Short, RadioRouter> routers = new HashMap<>();

    public static RadioManager getInstance() {
        if (INSTANCE == null) INSTANCE = new RadioManager();
        return INSTANCE;
    }

    public RadioManager() {}

    public static <R extends RadioRouter> void putRouter(@Nullable RouterContainer<R> container, R router) {
        if (container != null) {
            container.add(router);
        } else {
            pushRouter(router);
        }
    }

    public static short pushRouter(RadioRouter router) {
        return pushRouter(routers, router);
    }
    public static short pushRouter(Map<Short, RadioRouter> map, RadioRouter router) {
        for (short identifier = Short.MIN_VALUE; identifier < Short.MAX_VALUE; identifier++) {
            if (map.containsKey(identifier)) continue;

            router.identifier = identifier;
            map.put(identifier, router);

            return identifier;
        }

        return Short.MAX_VALUE;
    }

    public static short getIdentifier(Predicate<RadioRouter> filter) {
        Optional<Map.Entry<Short, RadioRouter>> result = routers.entrySet().stream().filter(entry -> filter.test(entry.getValue())).findFirst();
        return result.map(Map.Entry::getKey).orElse(Short.MAX_VALUE);
    }

    public static List<RadioRouter> getRouters() {
        return routers.values().stream().toList();
    }

    public static void removeRouter(RadioRouter router) {
        removeRouter(router::equals);
    }
    public static void removeRouter(Predicate<RadioRouter> criteria) {
        routers.entrySet().removeIf(entry -> criteria.test(entry.getValue()));
    }
    public static void removeRouter(short identifier) {
        routers.remove(identifier);
    }

    public static void removeRouter(UUID uuid) {
        removeRouter(router -> router.reference.equals(uuid));
    }
    public static void removeRouter(Entity owner) {
        removeRouter(router -> router.owner == owner);
    }
    public static void removeRouter(WorldlyPosition location) {
        removeRouter(router -> router.location != null && router.location.equals(location));
    }


    public static RadioRouter getRouter(Predicate<RadioRouter> filter) {
        Optional<Map.Entry<Short, RadioRouter>> result = routers.entrySet().stream().filter(entry -> filter.test(entry.getValue())).findFirst();
        return result.map(Map.Entry::getValue).orElse(null);
    }
    public static RadioRouter getRouter(short identifier) {
        return routers.get(identifier);
    }

    public static RadioRouter getRouter(UUID reference, @Nullable String type) {
        return getRouter(router ->
                router.reference.equals(reference) && (type == null ? router.getClass().equals(RadioRouter.class) : router.getClass().getSimpleName().equals(type))
        );
    }
    public static RadioRouter getRouter(UUID reference) {
        return getRouter(router -> router.reference.equals(reference));
    }
    public static RadioRouter getRouter(Entity owner) {
        return getRouter(router -> owner.equals(router.owner));
    }
    public static RadioRouter getRouter(WorldlyPosition location) {
        return getRouter(router -> location.equals(router.location));
    }

    public static void registerRouter(RadioRouter router) {
        putRouter(null, router);
    }

    public static RadioRouter getRouterSided(UUID reference, boolean isClient) {
        return isClient ? ClientRadioManager.getRouter(reference) : RadioManager.getRouter(reference);
    }
    public static RadioRouter getRouterSided(UUID reference, @Nullable String type, boolean isClient) {
        return isClient ? ClientRadioManager.getRouter(reference, type) : RadioManager.getRouter(reference, type);
    }
    public static void registerRouterSided(RadioRouter router, boolean isClient, @Nullable Frequency frequency) {
        CommonSimpleRadio.debug("Adding {} of reference {}", router.getClass().getSimpleName(), router.reference);
        if (isClient) {
            ClientRadioManager.registerRouter(router);
        } else {
            if (router instanceof RadioSpeaker speaker) {
                registerSpeaker(speaker);
            } else if (router instanceof RadioListener listener) {
                registerListener(listener);
            } else if (router instanceof RadioReceiver receiver) {
                if (frequency != null) frequency.registerReceiver(receiver);
            } else if (router instanceof RadioTransmitter transmitter) {
                if (frequency != null) frequency.registerTransmitter(transmitter);
            } else {
                RadioManager.registerRouter(router);
            }
        }
    }
    public static void removeRouterSided(UUID uuid, boolean isClient) {
        if (isClient) {
            ClientRadioManager.removeRouter(uuid);
        } else {
            RadioManager.removeRouter(uuid);
        }
    }
    public static void removeRouterSided(RadioRouter router, boolean isClient) {
        if (isClient) {
            ClientRadioManager.removeRouter(router);
        } else {
            RadioManager.removeRouter(router);
        }
    }

    // ---- Speakers ---- \\

    public static List<RadioSpeaker> getSpeakers() {
        return speakers.stream().toList();
    }

    public static void removeSpeaker(RadioSpeaker speaker) {
        removeSpeaker(speaker::equals);
    }
    public static void removeSpeaker(Predicate<RadioSpeaker> criteria) {
        speakers.removeIf(criteria);
    }
    public static void removeSpeaker(short identifier) {
        speakers.remove(identifier);
    }

    public static void removeSpeaker(Entity owner) {
        removeSpeaker(speaker -> owner.equals(speaker.owner));
    }
    public static void removeSpeaker(WorldlyPosition location) {
        removeSpeaker(speaker -> location.equals(speaker.location));
    }
    public static void removeSpeaker(UUID id) {
        removeSpeaker(speaker -> id.equals(speaker.reference));
    }

    public static RadioSpeaker getSpeaker(Entity owner) {
        return getSpeaker(speaker -> owner.equals(speaker.owner));
    }
    public static RadioSpeaker getSpeaker(WorldlyPosition location) {
        return getSpeaker(speaker -> location.equals(speaker.location));
    }
    public static RadioSpeaker getSpeaker(UUID id) {
        return getSpeaker(speaker -> id.equals(speaker.reference));
    }
    public static RadioSpeaker getSpeaker(Predicate<RadioSpeaker> filter) {
        Optional<RadioSpeaker> result = speakers.stream().filter(filter).findFirst();
        return result.orElse(null);
    }

    public static RadioSpeaker getOrCreateSpeaker(Entity owner, @Nullable UUID id) {
        boolean isClient = owner.level().isClientSide;

        RadioSpeaker speaker = isClient ? ClientRadioManager.getSpeaker(owner) : getSpeaker(owner);
        if (speaker == null) speaker = isClient ? ClientRadioManager.getSpeaker(id) : getSpeaker(id);

        return speaker != null ? speaker : new RadioSpeaker(owner, id);
    }

    public static RadioSpeaker getOrCreateSpeaker(Entity owner) { return getOrCreateSpeaker(owner, null); }
    public static RadioSpeaker getOrCreateSpeaker(WorldlyPosition location, @Nullable UUID id) {
        boolean isClient = location.level.isClientSide;

        RadioSpeaker speaker = isClient ? ClientRadioManager.getSpeaker(location) : getSpeaker(location);
        if (speaker == null) speaker = isClient ? ClientRadioManager.getSpeaker(id) : getSpeaker(id);

        return speaker != null ? speaker : new RadioSpeaker(location, id);
    }
    public static RadioSpeaker getOrCreateSpeaker(WorldlyPosition location) { return getOrCreateSpeaker(location, null); }

    public static RadioSpeaker registerSpeaker(RadioSpeaker speaker) {
        if (speaker.location != null) {
            if (speaker.location.isClientSide()) {
                CommonSimpleRadio.warn("Attempted to register a client-sided speaker on the server; cancelling");
                return null;
            }
        }

        putRouter(speakers, speaker);
        return speaker;
    }

    // ---- Listeners ---- \\

    public static List<RadioListener> getListeners() {
        return listeners.stream().toList();
    }

    public static void removeListener(RadioListener listener) {
        removeListener(listener::equals);
    }
    public static void removeListener(Predicate<RadioListener> criteria) {
        listeners.removeIf(criteria);
    }
    public static void removeListener(short identifier) {
        listeners.remove(identifier);
    }

    public static void removeListener(Entity owner) {
        removeListener(listener -> owner.equals(listener.owner));
    }
    public static void removeListener(WorldlyPosition location) {
        removeListener(listener -> location.equals(listener.location));
    }
    public static void removeListener(UUID id) {
        removeListener(listener -> id.equals(listener.reference));
    }

    public static RadioListener getListener(Entity owner) {
        return getListener(listener -> owner.equals(listener.owner));
    }
    public static RadioListener getListener(WorldlyPosition location) {
        return getListener(listener -> location.equals(listener.location));
    }
    public static RadioListener getListener(UUID id) {
        return getListener(listener -> id.equals(listener.reference));
    }
    public static RadioListener getListener(Predicate<RadioListener> filter) {
        Optional<RadioListener> result = listeners.stream().filter(filter).findFirst();
        return result.orElse(null);
    }

    public static RadioListener getOrCreateListener(Entity owner, @Nullable UUID id) {
        boolean isClient = owner.level().isClientSide;

        RadioListener listener = isClient ? ClientRadioManager.getListener(owner) : getListener(owner);
        if (listener == null) listener = isClient ? ClientRadioManager.getListener(id) : getListener(id);

        return listener != null ? listener : new RadioListener(owner, id);
    }
    public static RadioListener getOrCreateListener(Entity owner) { return getOrCreateListener(owner, null); }
    public static RadioListener getOrCreateListener(WorldlyPosition location, @Nullable UUID id) {
        boolean isClient = location.level.isClientSide;

        RadioListener listener = isClient ? ClientRadioManager.getListener(location) : getListener(location);
        if (listener == null) listener = isClient ? ClientRadioManager.getListener(id) : getListener(id);

        return listener != null ? listener : new RadioListener(location, id);
    }
    public static RadioListener getOrCreateListener(WorldlyPosition location) { return getOrCreateListener(location, null); }

    public static RadioListener registerListener(RadioListener listener) {
        if (listener.location != null) {
            if (listener.location.isClientSide()) {
                CommonSimpleRadio.warn("Attempted to register a client-sided listener on the server; cancelling");
                return null;
            }
        }

        putRouter(listeners, listener);
        return listener;
    }

    // -------- \\

    public static void close() {
        Frequency.close();

        speakers.clear();
        listeners.clear();
        routers.clear();
    }

    public static <R extends RadioRouter> void validate(List<R> container) {
        container.removeIf(Predicate.not(RadioRouter::validate));
        container.removeIf(entry -> entry.owner == null && entry.location == null);
    }

    public static void garbageCollect() {
        Frequency.garbageCollect();

        validate(speakers);
        validate(listeners);

        routers.entrySet().removeIf(entry -> !entry.getValue().validate());
        routers.entrySet().removeIf(entry -> entry.getValue().owner == null && entry.getValue().location == null);
    }

    public static void levelTick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            playerVelocities.compute(player.getUUID(), (uuid, vector) -> {
                if (vector == null) vector = new Vector3f();
                vector.set((float) (player.getX() - player.xOld), (float) (player.getY() - player.yOld), (float) (player.getZ() - player.zOld));
                return vector;
            });
        }
    }

    public static void serverTick(int tickCount) {
        if (tickCount % 20 == 0) {
            garbageCollect();
        }

        // -- Receiver, Transmitter and Listener ticking -- \\
        List<Frequency> frequencies = Frequency.getFrequencies();
        for (Frequency frequency : frequencies) {
            frequency.serverTick(tickCount);
        }
        Frequency.applyModifications();

        for (RadioListener listener : listeners) {
            listener.tick(tickCount);
        }
        for (RadioSpeaker speaker : speakers) {
            speaker.tick(tickCount);
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

    private static void applyModifications() {
        for (int i = 0; i < pendingModifications.size(); i++) {
            Runnable modification = pendingModifications.poll();
            if (modification == null) break;
            modification.run();
        }
    }

    public static void queueSource(RadioSource source, RadioRouter destination, int delay) {
        pendingSources.add(new QueuedSource(source, destination, delay));
    }
    public static void dequeueSource(Predicate<QueuedSource> criteria) {
        pendingSources.removeIf(criteria);
        sourceQueue.removeIf(criteria);
    }

    public static boolean readQueue(Predicate<QueuedSource> filter) {
        for (QueuedSource source : sourceQueue) {
            if (filter.test(source)) return true;
        }
        for (QueuedSource source : pendingSources) {
            if (filter.test(source)) return true;
        }

        return false;
    }

    public enum CollectionResult {
        PASS,
        IGNORE,
        COLLECT
    }

    public static boolean verifyLocationCollection(WorldlyPosition position, Class<?> clazz) {
        BlockPos pos = position.realLocation();

        CollectionResult result = CompatCore.verifyLocationCollection(position, clazz);
        if (result == CollectionResult.IGNORE) {
            return true;
        } else if (result == CollectionResult.COLLECT) {
            return false;
        }

        if (!position.level.isLoaded(pos)) return false;

        BlockState state = position.level.getBlockState(pos);
        if (state.isAir()) return false;

        Block block = state.getBlock();
        return clazz.isInstance(block) || clazz.isInstance(block.asItem());
    }

    public static boolean verifyEntityCollection(Entity entity, Predicate<ItemStack> itemCriteria) {
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
    public static ItemStack isEntityHolding(Entity entity, Predicate<ItemStack> handCriteria) {
        for (ItemStack stack : entity.getHandSlots()) {
            if (handCriteria.test(stack)) return stack;
        }
        return null;
    }

    public static TreeMap<Float, RadioListener> getListeners(Vector3f at) {
        TreeMap<Float, RadioListener> qualified = new TreeMap<>();
        for (RadioListener listener : getListeners()) {
            Vector3f position;
            if (listener.location != null) {
                position = listener.location.position();
            } else if (listener.owner != null) {
                position = listener.owner.position().toVector3f();
            } else continue;

            float distance = position.distance(at);
            if (distance > listener.range) continue;

            qualified.put(distance, listener);
        }
        return qualified;
    }

    // --- Audio Gathering --- \\

    public void onSoundPlayed(ServerLevel level, Vec3 location, Holder<SoundEvent> soundHolder, float volume, float pitch, long seed) {
        onSoundPlayed(level, location, soundHolder, volume, pitch, 0, seed);
    }
    public void onSoundPlayed(ServerLevel level, Vec3 location, Holder<SoundEvent> soundHolder, float volume, float pitch, float offset, long seed) {
        if (level.isClientSide) return;
        if (!SimpleRadioLibrary.SERVER_CONFIG.frequency.soundListening) return;
        if (!level.isLoaded(BlockPos.containing(location))) return;

        SoundEvent sound = soundHolder.value();

        TreeMap<Float, RadioListener> qualified = getListeners(location.toVector3f());
        for (Map.Entry<Float, RadioListener> entry : qualified.entrySet()) {
            float distance = entry.getKey();
            RadioListener listener = entry.getValue();

            double falloff = CommonRadioPlugin.getFalloff(distance, listener.range);

            RadioSource newSource = new RadioSource(
                    listener.reference,
                    WorldlyPosition.of(location.toVector3f(), level),
                    sound,
                    (float) (falloff * volume)
            );
            newSource.pitch = pitch;
            newSource.offset = offset;
            newSource.seed = seed;

            listener.onSource(newSource);
        }
    }

    public void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;

        ServerPlayer sender = (ServerPlayer) senderConnection.getPlayer().getPlayer();
        ServerLevel level = sender.serverLevel();

        TreeMap<Float, RadioListener> qualified = getListeners(new Vector3f((float) sender.getX(), (float) sender.getY(), (float) sender.getZ()));

        for (Map.Entry<Float, RadioListener> entry : qualified.entrySet()) {
            float distance = entry.getKey();
            RadioListener listener = entry.getValue();

            double falloff = CommonRadioPlugin.getFalloff(distance, listener.range);

            Vector3f listenerPosition = null;
            if (listener.location != null) {
                listenerPosition = listener.location.position();
            } else if (listener.owner != null) {
                listenerPosition = listener.owner.position().toVector3f();
            }
            if (listenerPosition == null) continue;

            byte[] data = event.getPacket().getOpusEncodedData();
            Vector3f senderPosition = sender.position().toVector3f();
            RadioSource newSource = new RadioSource(
                    sender.getUUID(),
                    WorldlyPosition.of(senderPosition, level),
                    data,
                    (float) falloff
            );

            // Decoding for initial reading
            OpusDecoder decoder = listener.getDecoder(sender.getUUID());
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
