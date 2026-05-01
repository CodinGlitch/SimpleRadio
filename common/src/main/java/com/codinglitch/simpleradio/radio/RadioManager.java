package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.*;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.Wiring;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.core.Listeners;
import com.codinglitch.simpleradio.core.SimpleRadioEvent;
import com.codinglitch.simpleradio.core.Speakers;
import com.codinglitch.simpleradio.core.central.FrequencyChannel;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.core.registry.blocks.InsulatorBlock;
import com.codinglitch.simpleradio.core.registry.blocks.InsulatorBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RadioManager extends ServerSimpleRadioApi {
    public static final RadioManager INSTANCE = new RadioManager();

    private static final Map<Consumer<? extends SimpleRadioEvent>, Class<? extends SimpleRadioEvent>> EVENT_LISTENERS = new HashMap<>();

    private static final FrequenciesImpl FREQUENCIES = new FrequenciesImpl();
    private static final SpeakersImpl SPEAKERS = new SpeakersImpl();
    private static final ListenersImpl LISTENERS = new ListenersImpl();


    // double queue for the win
    private static final List<QueuedSource> pendingSources = new ArrayList<>();
    private static final List<QueuedSource> sourceQueue = new ArrayList<>();

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

    public static void load() {
    }

    public static RadioManager getInstance() {
        return INSTANCE;
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
    public <E extends SimpleRadioEvent> void listen(Class<E> event, Consumer<E> listener) {
        EVENT_LISTENERS.put(listener, event);
    }

    public static <E extends SimpleRadioEvent> void post(E event) {
        Class<?> type = event.getClass();
        for (Map.Entry<Consumer<? extends SimpleRadioEvent>, Class<? extends SimpleRadioEvent>> entry : EVENT_LISTENERS.entrySet()) {
            if (!type.equals(entry.getValue())) continue;
            Consumer<E> listener = (Consumer<E>) entry.getKey();
            listener.accept(event);
        }
    }

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
    public <T> Optional<T> getConfig(String path) {
        return CommonSimpleRadio.getConfigFrom(SimpleRadioLibrary.SERVER_CONFIG, path);
    }

    @Override
    public <T> void setConfig(String path, T value) {
        CommonSimpleRadio.setConfigFrom(SimpleRadioLibrary.SERVER_CONFIG, path, value);
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
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(offsetPos);

            if (blockEntity instanceof InsulatorBlockEntity insulatorBlockEntity) {
                List<Wiring> wires = insulatorBlockEntity.getWires();
                if (wires.isEmpty()) continue;

                Wiring wire = wires.get(0);
                Router router = wire.transport(insulatorBlockEntity.getRouter());
                if (router == null) continue;

                BlockPos routerPos = router.getLocation().blockPos();

                BlockState blockState = level.getBlockState(routerPos);
                if (!(blockState.getBlock() instanceof InsulatorBlock)) continue;

                Direction routerDirection = blockState.getValue(InsulatorBlock.FACING);
                return routerPos.relative(routerDirection.getOpposite());
            }
        }

        return pos;
    }

    @Override
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
    public Router removeRouter(Router router) {
        if (router == null) return null;
        return removeRouter(router::equals);
    }
    @Override
    public Router removeRouter(Predicate<Router> criteria) {
        List<Map.Entry<Short, Router>> removal = routers.entrySet().stream()
                .filter(entry -> criteria.test(entry.getValue()))
                .toList();

        if (removal.isEmpty()) return null;

        removal.forEach(entry -> {
            entry.getValue().invalidate();
            routers.entrySet().remove(entry);
        });
        return removal.stream().findFirst().get().getValue();
    }
    @Override
    public Router removeRouter(short identifier) {
        return routers.remove(identifier);
    }

    @Override
    public Router removeRouter(UUID uuid) {
        return removeRouter(router -> router.getReference().equals(uuid));
    }
    @Override
    public Router removeRouter(Entity owner) {
        return removeRouter(router -> router.getOwner() == owner);
    }
    @Override
    public Router removeRouter(WorldlyPosition location) {
        return removeRouter(router -> router.getPosition() != null && router.getPosition().equals(location));
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
        RadioRouter radioRouter = (RadioRouter) router;
        for (short identifier = Short.MIN_VALUE; identifier < Short.MAX_VALUE; identifier++) {
            if (routers.containsKey(identifier)) continue;

            radioRouter.identifier = identifier;
            routers.put(identifier, radioRouter);

            return;
        }
    }

    @Override
    public void registerRouter(Router router, @Nullable Frequency frequency) {
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

    // -------- \\

    public static void close() {
        FREQUENCIES.close();

        SPEAKERS.close();
        LISTENERS.close();

        routers.clear();
    }

    public static <R extends Router> void validate(RouterContainer<R> container) {
        container.garbageCollect(Predicate.not(Router::validate));
        container.garbageCollect(entry -> entry.getOwner() == null && entry.getPosition() == null);
    }

    public static void garbageCollect() {
        FREQUENCIES.garbageCollect();

        SPEAKERS.garbageCollect();
        LISTENERS.garbageCollect();

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
            if (source == null) {
                iterator.remove();
                continue;
            }

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
            if (source == null) continue;
            if (filter.test(source)) return true;
        }
        for (QueuedSource source : pendingSources) {
            if (source == null) continue;
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

    @Override
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

    @Override
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
        } else if (entity instanceof LivingEntity livingEntity) {
            for (ItemStack stack : livingEntity.getHandSlots()) {
                if (itemCriteria.test(stack)) return true;
            }
        }
        return false;
    }

    @Nullable
    public ItemStack isEntityHolding(Entity entity, Predicate<ItemStack> handCriteria) {
        if (entity instanceof LivingEntity livingEntity) {
            for (ItemStack stack : livingEntity.getHandSlots()) {
                if (handCriteria.test(stack)) return stack;
            }
        }
        return null;
    }



    // --- Audio Gathering --- \\

    // I mixin here instead of using the appropriate events to access the channel as well as prevent duplicates
    public void onLocationalPacket(Level level, LocationalAudioChannel channel, byte[] data) {

        String category = channel.getCategory();
        if (category != null) {
            if (category.equals("speakers") || category.equals("transceivers") || category.equals("radios") || category.equals("walkies")) {
                if (!SimpleRadioLibrary.SERVER_CONFIG.router.feedbackListening) return;
            } else {
                if (category.equals("music_discs") || category.equals("note_blocks") || category.equals("goat_horns"))
                    if (!SimpleRadioLibrary.SERVER_CONFIG.compatibilities.audioplayer.enabled) return;
            }
        }


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

    public void sendRecord(ItemStack stack, WorldlyPosition position, long identifier) {
        RadioManager.getInstance().sendSound(
                position,
                CompatCore.getSound(position.level.registryAccess(), stack),
                1, 1, identifier
        );
    }
    public void stopRecord(ServerLevel level, long identifier) {

        Map<Listener, Source> sources = new HashMap<>();
        for (Listener listener : LISTENERS.get()) {
            RadioSource newSource = new RadioSource(
                    listener.getReference(),
                    listener.getLocation(),
                    "", 0
            );
            newSource.seed = identifier;

            sources.put(listener, newSource);
        }

        level.getServer().execute(() -> sources.forEach(Listener::listen));
    }
    public void updateRecord(ItemStack stack, WorldlyPosition position, float offset, long identifier) {
        RadioManager.getInstance().sendSound(
                position,
                CompatCore.getSound(position.level.registryAccess(), stack),
                1, 1,  offset, identifier
        );
    }

    public void sendSound(WorldlyPosition location, SoundEvent soundEvent, float volume, float pitch, long seed) {
        sendSound(location, soundEvent, volume, pitch, 0, seed);
    }
    public void sendSound(WorldlyPosition location, SoundEvent soundEvent, float volume, float pitch, float offset, long seed) {
        sendSound(location, soundEvent.getLocation().toString(), volume, pitch, offset, seed);
    }
    public void sendSound(WorldlyPosition location, String sound, float volume, float pitch, long seed) {
        sendSound(location, sound, volume, pitch, 0, seed);
    }
    public void sendSound(WorldlyPosition location, String sound, float volume, float pitch, float offset, long seed) {
        Level level = location.level;

        if (level.isClientSide) return;
        if (!SimpleRadioLibrary.SERVER_CONFIG.router.soundListening) return;
        if (!level.isLoaded(location.blockPos())) return;

        Map<Float, Listener> qualified = LISTENERS.getAt(location);

        Map<Listener, Source> sources = new HashMap<>();
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

            sources.put(listener, newSource);
        }

        level.getServer().execute(() -> sources.forEach(Listener::listen));
    }

    @Override
    public void sendAudio(WorldlyPosition location, UUID sender, byte[] data) {
        Level level = location.level;
        Map<Float, Listener> qualified = LISTENERS.getAt(location);

        Map<Listener, Source> sources = new HashMap<>();
        for (Map.Entry<Float, Listener> entry : qualified.entrySet()) {
            float distance = entry.getKey();
            RadioListener listener = (RadioListener) entry.getValue();

            double falloff = CommonRadioPlugin.getFalloff(distance, listener.range);

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

            sources.put(listener, newSource);
        }

        level.getServer().execute(() -> sources.forEach(Listener::listen));
    }
}
