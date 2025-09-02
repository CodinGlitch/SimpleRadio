package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundSpeakSoundPacket;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import com.codinglitch.simpleradio.routers.Speaker;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioSource}s and emits them in-world.
 * <br>
 * Often serves as the end of the audio pipeline.
 * <br>
 * <b>Does not route further.</b>
 */
public class RadioSpeaker extends RadioRouter implements Supplier<short[]>, Speaker {
    // migrated to locational audio channels only due to alternatives not having range property
    public LocationalAudioChannel audioChannel;
    public AudioPlayer audioPlayer;
    private final Map<UUID, Map<UUID, Queue<short[]>>> packetBuffer;
    private final Map<UUID, OpusDecoder> decoders;
    private final AudioEffect effect;

    public String category;
    public float range = 8;

    public int speakingTime = 0;

    protected RadioSpeaker(UUID id) {
        super(id);

        packetBuffer = new ConcurrentHashMap<>();
        decoders = new ConcurrentHashMap<>();
        effect = new BaseAudioEffect();
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

    @Override
    public int getSpeakingTime() {
        return speakingTime;
    }

    @Override
    public short[] get() {
        short[] audio = generatePacket();
        if (audio == null) {
            if (audioPlayer != null)
                audioPlayer.stopPlaying();

            audioPlayer = null;
            return null;
        }
        return audio;
    }

    public short[] generatePacket() {
        List<short[]> totalPacketsToCombine = new ArrayList<>();

        for (Map.Entry<UUID, Map<UUID, Queue<short[]>>> listenerPacket : packetBuffer.entrySet()) {
            Map<UUID, Queue<short[]>> playerPackets = listenerPacket.getValue();
            if (playerPackets.isEmpty()) continue;

            List<short[]> playerPacketsToCombine = new ArrayList<>();
            for (Map.Entry<UUID, Queue<short[]>> playerPacket : playerPackets.entrySet()) {
                short[] audio = playerPacket.getValue().poll();
                if (audio != null) playerPacketsToCombine.add(audio);
            }
            playerPackets.values().removeIf(Queue::isEmpty);

            totalPacketsToCombine.add(CommonRadioPlugin.combineAudio(playerPacketsToCombine));
        }
        packetBuffer.values().removeIf(Map::isEmpty);

        if (totalPacketsToCombine.isEmpty()) return null;

        return CommonRadioPlugin.combineAudio(totalPacketsToCombine);
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

        if (speakingTime > 0) speakingTime--;
    }

    @Override
    public void take(Source source) {
        if (!this.active) return;
        if (acceptCriteria != null && !acceptCriteria.test(source)) return;
        super.take(source);
        speak(source);
    }

    public void speak(Source source) {
        this.compileActivity(source);
        RadioSource radioSource = (RadioSource) source;

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

        if (!SimpleRadioLibrary.SERVER_CONFIG.frequency.crossDimensional && level != radioSource.origin.level) return;

        this.effect.severity = (float) radioSource.computeSeverity();
        this.effect.volume = radioSource.volume;
        if (this.effect.severity >= 100) return;

        // Parsing sound event
        if (radioSource.data == null) {
            if (radioSource.sound == null) return;

            for (ServerPlayer player : level.players()) {
                if (player.position().distanceTo(new Vec3(position)) < 50) {
                    Services.NETWORKING.sendToPlayer(player, new ClientboundSpeakSoundPacket(
                            this.getReference(), radioSource.sound,
                            radioSource.volume, radioSource.pitch, this.effect.severity, radioSource.offset, radioSource.seed
                    ));
                }
            }

            return;
        }

        // Packet buffer
        Map<UUID, Queue<short[]>> listenerPackets = packetBuffer.computeIfAbsent(radioSource.owner, k -> new ConcurrentHashMap<>());
        Queue<short[]> playerPackets = listenerPackets.computeIfAbsent(radioSource.getRealOwner(), k -> new LinkedList<>());
        if (playerPackets.isEmpty()) {
            for (int i = 0; i < SimpleRadioLibrary.SERVER_CONFIG.frequency.packetBuffer; i++) {
                //playerPackets.offer(null);
            }
        }

        // Decoding
        byte[] data = radioSource.data;

        OpusDecoder decoder = getDecoder(radioSource.owner);
        if (data == null || data.length == 0) {
            decoder.resetState();
            return;
        }
        short[] decoded = decoder.decode(data);
        short[] filtered = effect.apply(decoded);

        if (!CommonRadioPlugin.isAudioValid(filtered)) return;
        playerPackets.offer(filtered);

        // Loader-specific compat
        Services.COMPAT.onData(this, radioSource, decoded);

        // Common compat
        CompatCore.onData(this, radioSource, decoded);

        if (this.audioPlayer == null)
            getAudioPlayer().startPlaying();
    }

    public OpusDecoder getDecoder(UUID sender) {
        return decoders.computeIfAbsent(sender, uuid -> CommonRadioPlugin.serverApi.createDecoder());
    }

    private AudioPlayer getAudioPlayer() {
        if (this.audioPlayer == null) {

            WorldlyPosition location = this.getLocation();
            this.audioChannel = CommonRadioPlugin.serverApi.createLocationalAudioChannel(this.reference,
                    CommonRadioPlugin.serverApi.fromServerLevel(location.level),
                    CommonRadioPlugin.serverApi.createPosition(location.x + 0.5, location.y + 0.5, location.z + 0.5)
            );
            audioChannel.setDistance(range);
            audioChannel.setCategory(category);

            this.audioPlayer = CommonRadioPlugin.serverApi.createAudioPlayer(audioChannel, CommonRadioPlugin.serverApi.createEncoder(), this);
        }
        return this.audioPlayer;
    }

    @Override
    public void invalidate() {
        if (this.audioPlayer != null)
            this.audioPlayer.stopPlaying();

        super.invalidate();
    }
}
