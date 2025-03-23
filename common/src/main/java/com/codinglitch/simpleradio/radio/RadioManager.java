package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
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
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class RadioManager {
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
    private static final List<RadioSpeaker> speakers = new ArrayList<>();
    private static final List<RadioListener> listeners = new ArrayList<>();
    private static final List<RadioRouter> routers = new ArrayList<>();
    public static RadioManager getInstance() {
        if (INSTANCE == null) INSTANCE = new RadioManager();
        return INSTANCE;
    }

    public RadioManager() {}

    // ---- Speakers ---- \\

    public static List<RadioSpeaker> getSpeakers() {
        return speakers;
    }

    public static void removeSpeaker(RadioSpeaker speaker) {
        speakers.remove(speaker);
    }

    public static void removeSpeaker(Entity owner) {
        speakers.removeIf(speaker -> owner.equals(speaker.owner));
    }
    public static void removeSpeaker(WorldlyPosition location) {
        speakers.removeIf(speaker -> location.equals(speaker.location));
    }
    public static void removeSpeaker(UUID id) {
        speakers.removeIf(speaker -> id.equals(speaker.id));
    }

    public static RadioSpeaker getSpeaker(Entity owner) {
        return speakers.stream().filter(speaker -> owner.equals(speaker.owner))
                .findFirst().orElse(null);
    }
    public static RadioSpeaker getSpeaker(WorldlyPosition location) {
        return speakers.stream().filter(speaker -> location.equals(speaker.location))
                .findFirst().orElse(null);
    }
    public static RadioSpeaker getSpeaker(UUID id) {
        RadioSpeaker s = speakers.stream().filter(speaker -> id.equals(speaker.id)).findFirst().orElse(null);
        return s;
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

        pendingModifications.add(() -> speakers.add(speaker));
        return speaker;
    }

    // ---- Listeners ---- \\

    public static List<RadioListener> getListeners() {
        return listeners;
    }

    public static void removeListener(RadioListener listener) {
        listeners.remove(listener);
    }
    public static void removeListener(Entity owner) {
        listeners.removeIf(listener -> owner.equals(listener.owner));
    }
    public static void removeListener(WorldlyPosition location) {
        listeners.removeIf(listener -> location.equals(listener.location));
    }
    public static void removeListener(UUID id) {
        listeners.removeIf(listener -> id.equals(listener.id));
    }

    public static RadioListener getListener(Entity owner) {
        return listeners.stream().filter(listener -> owner.equals(listener.owner))
                .findFirst().orElse(null);
    }
    public static RadioListener getListener(WorldlyPosition location) {
        return listeners.stream().filter(listener -> location.equals(listener.location))
                .findFirst().orElse(null);
    }
    public static RadioListener getListener(UUID id) {
        return listeners.stream().filter(listener -> id.equals(listener.id))
                .findFirst().orElse(null);
    }
    public static Optional<RadioListener> tryGetListener(UUID id) {
        return listeners.stream().filter(listener -> id.equals(listener.id)).findFirst();
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

        pendingModifications.add(() -> listeners.add(listener));
        return listener;
    }

    // -------- \\

    public static void close() {
        Frequency.close();

        speakers.clear();
        listeners.clear();
        routers.clear();
    }

    public static void garbageCollect() {
        Frequency.garbageCollect();

        speakers.removeIf(Predicate.not(RadioSpeaker::validate));
        speakers.removeIf(speaker -> speaker.owner == null && speaker.location == null);

        listeners.removeIf(Predicate.not(RadioListener::validate));
        listeners.removeIf(listener -> listener.owner == null && listener.location == null);

        routers.removeIf(Predicate.not(RadioRouter::validate));
        routers.removeIf(speaker -> speaker.owner == null && speaker.location == null);
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

        for (RadioListener listener : getListeners()) {
            listener.tick(tickCount);
        }
        for (RadioSpeaker speaker : getSpeakers()) {
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

    public static List<RadioRouter> getRouters() {
        return routers;
    }
    public static RadioRouter getRouter(UUID uuid) {
        return routers.stream().filter(router -> router.id.equals(uuid)).findFirst().orElse(null);
    }
    public static RadioRouter getRouter(Entity owner) {
        return routers.stream().filter(router -> owner.equals(router.owner))
                .findFirst().orElse(null);
    }
    public static RadioRouter getRouter(WorldlyPosition location) {
        return routers.stream().filter(router -> location.equals(router.location))
                .findFirst().orElse(null);
    }
    public static void registerRouter(RadioRouter router) {
        pendingModifications.add(() -> routers.add(router));
    }
    public static void removeRouter(RadioRouter router) {
        routers.removeIf(router1 -> router1.equals(router));
    }
    public static void removeRouter(UUID uuid) {
        routers.removeIf(router -> router.id.equals(uuid));
    }
    public static void removeRouter(Entity owner) {
        routers.removeIf(router -> router.owner == owner);
    }
    public static void removeRouter(WorldlyPosition location) {
        routers.removeIf(router -> router.location != null && router.location.equals(location));
    }

    public static RadioRouter getRouterSided(UUID uuid, boolean isClient) {
        return isClient ? ClientRadioManager.getRouter(uuid) : RadioManager.getRouter(uuid);
    }
    public static void registerRouterSided(RadioRouter router, boolean isClient, @Nullable Frequency frequency) {
        if (isClient) {
            ClientRadioManager.registerRouter(router);
        } else {
            if (router instanceof RadioSpeaker speaker) {
                registerSpeaker(speaker);
            } else if (router instanceof RadioListener listener) {
                registerListener(listener);
            } else if (router instanceof RadioReceiver receiver) {
                if (frequency != null) frequency.queueReceiver(receiver);
            } else if (router instanceof RadioTransmitter transmitter) {
                if (frequency != null) frequency.queueTransmitter(transmitter);
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
                    listener.id,
                    WorldlyPosition.of(location.toVector3f(), level),
                    sound,
                    (float) (falloff * volume)
            );
            newSource.pitch = pitch;
            newSource.offset = offset;
            newSource.seed = seed;

            listener.onData(newSource);
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

            //Vector3f senderVelocity = playerVelocities.get(sender.getUUID());
            Vector3f senderPosition = sender.position().toVector3f();

            ///double dopplerFactor = CommonRadioPlugin.getDoppler(listenerPosition, listener.velocity, senderPosition, senderVelocity);

            RadioSource newSource = new RadioSource(
                    sender.getUUID(),
                    WorldlyPosition.of(senderPosition, level),
                    event.getPacket().getOpusEncodedData(),
                    (float) falloff
            );
            //newSource.pitch = (float) dopplerFactor;

            listener.onData(newSource);
        }
    }
}
