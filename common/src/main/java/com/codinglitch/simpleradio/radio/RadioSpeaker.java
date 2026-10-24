package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundSpeakSoundPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioAudioEffects;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.routers.Speaker;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioMessage}s and emits them in-world.
 * <br>
 * Often serves as the end of the audio pipeline.
 * <br>
 * <b>Does not route further.</b>
 */
public class RadioSpeaker extends RadioRouter implements Speaker {
    // migrated to locational audio channels only due to alternatives not having range property
    public LocationalAudioChannel audioChannel;
    private OpusEncoder encoder;

    private final Map<UUID, Map<UUID, Queue<short[]>>> packetBuffer;
    private final AudioFilter effect;

    public String category;
    public float range = 8;

    public boolean isSpeaking = false;
    public int gracePeriod = 0;

    protected RadioSpeaker(UUID id) {
        super(id);

        packetBuffer = new ConcurrentHashMap<>();
        effect = SimpleRadioAudioEffects.RADIO.make(); // easier to dynamically change
    }
    protected RadioSpeaker() {
        this(UUID.randomUUID());
    }

    public RadioSpeaker(Entity owner) {
        this(owner, UUID.randomUUID());
    }
    public RadioSpeaker(Entity owner, UUID uuid) {
        this(uuid);
        this.owner = owner;

        SimpleRadioApi.registerRouterSided(this, owner.level().isClientSide(), null);
    }
    public RadioSpeaker(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioSpeaker(WorldlyPosition location, UUID uuid) {
        this(uuid);
        this.position = location;

        SimpleRadioApi.registerRouterSided(this, location.isClientSide(), null);
    }

    @Override
    public float getRange() {
        return range;
    }

    @Override
    public void setRange(float range) {
        this.range = range;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Starts streaming audio data through this speaker.
     * Creates the audio channel and encoder.
     */
    public void open() {
        isSpeaking = true;
        encoder = CommonRadioPlugin.serverApi.createEncoder();

        CommonSimpleRadio.info("opened");

        WorldlyPosition location = this.getLocation();
        this.audioChannel = CommonRadioPlugin.serverApi.createLocationalAudioChannel(this.reference,
                CommonRadioPlugin.serverApi.fromServerLevel(location.level),
                CommonRadioPlugin.serverApi.createPosition(location.x + 0.5, location.y + 0.5, location.z + 0.5)
        );
        audioChannel.setDistance(range);
        audioChannel.setCategory(category);
    }


    public void close() {
        if (encoder != null) encoder.close();
        if (audioChannel != null) audioChannel.flush();

        CommonSimpleRadio.info("closed");

        encoder = null;
        isSpeaking = false;
    }

    public short[] generatePacket() {
        List<short[]> packets = new ArrayList<>();

        for (Map.Entry<UUID, Map<UUID, Queue<short[]>>> listenerPacket : packetBuffer.entrySet()) {
            Map<UUID, Queue<short[]>> playerPackets = listenerPacket.getValue();
            if (playerPackets.isEmpty()) continue;

            for (Map.Entry<UUID, Queue<short[]>> playerPacket : playerPackets.entrySet()) {
                short[] audio = playerPacket.getValue().poll();
                if (audio != null) {
                    packets.add(audio);
                }
            }
            playerPackets.values().removeIf(Queue::isEmpty);

        }
        packetBuffer.values().removeIf(Map::isEmpty);

        if (packets.isEmpty()) return null;
        return CommonRadioPlugin.combineAudio(packets);
    }

    @Override
    public void dispatch() {
        if (!isSpeaking) return;

        CommonSimpleRadio.info("generating");
        short[] audio = generatePacket();
        if (audio == null) {
            gracePeriod--;
            if (gracePeriod > 0) return;

            close();
            return;
        }

        if (audio.length != 960) {
            return;
        }

        gracePeriod = 10;

        CommonSimpleRadio.info("encoding");
        byte[] encoded = this.encoder.encode(audio);

        CommonSimpleRadio.info("sending");
        this.audioChannel.send(encoded);
        CommonSimpleRadio.info("done");
    }

    @Override
    public void accept(Message message) {
        this.take(message);
    }

    @Override
    public void updateLocation(WorldlyPosition location) {
        super.updateLocation(location);

        if (audioChannel != null) {
            audioChannel.updateLocation(CommonRadioPlugin.serverApi.createPosition(location.x, location.y, location.z));
        }
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
    }

    @Override
    public void take(Message message) {
        if (!this.active) return;
        if (acceptCriteria != null && !acceptCriteria.test(message)) return;
        super.take(message);
        speak(message);
    }

    public void speak(Message message) {
        this.compileActivity(message);
        Source source = message.getSource();

        // Severity calculation
        ServerLevel level = null;
        Vector3f position = null;
        if (this.position != null) {
            level = (ServerLevel) this.position.level;
            position = this.position.position();
        } else {
            level = (ServerLevel) owner.level();
            position = owner.position().toVector3f();
        }
        if (level == null || position == null) return;

        if (!SimpleRadioLibrary.SERVER_CONFIG.frequency.crossDimensional && level != message.getOrigin().level) return;

        this.effect.setSeverity((float) message.computeSeverity());
        this.effect.volume = message.getVolume();
        if (this.effect.getSeverity() >= 100) return;

        // Parsing sound event
        if (source.getSound() != null) {
            for (ServerPlayer player : level.players()) {
                if (player.position().distanceTo(new Vec3(position)) < 50) {
                    Services.NETWORKING.sendToPlayer(player, new ClientboundSpeakSoundPacket(
                            this.getReference(), source.getSound(),
                            message.getVolume(), message.getPitch(), this.effect.getSeverity(), message.getOffset(), message.getSeed()
                    ));
                }
            }

            return;
        }

        // Packet buffer
        Map<UUID, Queue<short[]>> listenerPackets = packetBuffer.computeIfAbsent(message.getOwner(), k -> new ConcurrentHashMap<>());
        Queue<short[]> playerPackets = listenerPackets.computeIfAbsent(message.getRealOwner(), k -> new ArrayDeque<>());

        // Decoding
        short[] decoded = source.getDecoded(message.getRealOwner()).clone();
        short[] filtered = effect.apply(decoded);

        if (!CommonRadioPlugin.isAudioValid(decoded)) return;
        playerPackets.offer(decoded);

        // Loader-specific compat
        Services.COMPAT.onData(this, (RadioMessage) message, decoded);

        // Common compat
        CompatCore.onData(this, (RadioMessage) message, decoded);

        if (!isSpeaking)
            open();
    }

    @Override
    public void invalidate() {
        close();
        super.invalidate();
    }
}
