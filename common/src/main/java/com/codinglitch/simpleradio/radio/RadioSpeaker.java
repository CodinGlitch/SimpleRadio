package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RadioSpeaker extends RadioRouter implements Supplier<short[]> {
    private static final List<RadioSpeaker> speakers = new ArrayList<>();

    public static List<RadioSpeaker> getSpeakers() {
        return speakers;
    }

    public static void removeSpeaker(RadioSpeaker speaker) {
        speakers.remove(speaker);
    }
    public static void removeSpeaker(Entity owner) {
        speakers.removeIf(speaker -> speaker.owner == owner);
    }
    public static void removeSpeaker(WorldlyPosition location) {
        speakers.removeIf(speaker -> speaker.location != null && speaker.location.equals(location));
    }
    public static void removeSpeaker(UUID id) {
        speakers.removeIf(speaker -> speaker.id == id);
    }

    public static RadioSpeaker getSpeaker(Entity owner) {
        return speakers.stream().filter(speaker -> speaker.owner == owner)
                .findFirst().orElse(null);
    }
    public static RadioSpeaker getSpeaker(WorldlyPosition location) {
        return speakers.stream().filter(speaker -> speaker.location == location)
                .findFirst().orElse(null);
    }
    public static RadioSpeaker getSpeaker(UUID id) {
        return speakers.stream().filter(speaker -> speaker.id == id)
                .findFirst().orElse(null);
    }

    public static RadioSpeaker getOrCreateSpeaker(Entity owner, @Nullable UUID id) {
        RadioSpeaker speaker = getSpeaker(owner);
        if (speaker == null) speaker = getSpeaker(id);

        return speaker != null ? speaker : new RadioSpeaker(owner);
    }
    public static RadioSpeaker getOrCreateSpeaker(Entity owner) { return getOrCreateSpeaker(owner, null); }

    public static RadioSpeaker getOrCreateSpeaker(WorldlyPosition location, @Nullable UUID id) {
        RadioSpeaker speaker = getSpeaker(location);
        if (speaker == null) speaker = getSpeaker(id);

        return speaker != null ? speaker : new RadioSpeaker(location);
    }
    public static RadioSpeaker getOrCreateSpeaker(WorldlyPosition location) { return getOrCreateSpeaker(location, null); }

    public static void garbageCollect() {
        speakers.removeIf(Predicate.not(RadioSpeaker::validate));
        speakers.removeIf(speaker -> speaker.owner == null && speaker.location == null);
    }

    public AudioChannel audioChannel;
    public AudioPlayer audioPlayer;
    private final Map<UUID, List<short[]>> packetBuffer;
    private final Map<UUID, OpusDecoder> decoders;
    private final AudioEffect effect;

    public float range = 8;

    protected RadioSpeaker(UUID id) {
        super(id);
        speakers.add(this);

        packetBuffer = new HashMap<>();
        decoders = new HashMap<>();
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
    }
    public RadioSpeaker(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioSpeaker(WorldlyPosition location, UUID uuid) {
        this(uuid);
        this.location = location;
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
        List<short[]> packetsToCombine = new ArrayList<>();
        for (Map.Entry<UUID, List<short[]>> packets : packetBuffer.entrySet()) {
            if (packets.getValue().isEmpty()) continue;
            short[] audio = packets.getValue().remove(0);
            packetsToCombine.add(audio);
        }
        packetBuffer.values().removeIf(List::isEmpty);

        if (packetsToCombine.isEmpty()) return null;

        short[] combinedAudio = CommonRadioPlugin.combineAudio(packetsToCombine);

        return effect.apply(combinedAudio);
    }

    public void updateLocation(WorldlyPosition location) {
        super.updateLocation(location);
        if (this.audioChannel instanceof LocationalAudioChannel locationalAudioChannel) {
            locationalAudioChannel.updateLocation(CommonRadioPlugin.serverApi.createPosition(location.x, location.y, location.z));
        }
    }

    @Override
    public void accept(RadioSource source) {
        super.accept(source);
        speak(source);
    }

    public void speak(RadioSource source) {
        // Severity calculation
        ServerLevel level = null;
        Vector3f position = null;
        if (location != null) {
            level = (ServerLevel) location.level;
            position = location.position();
        } else {
            level = (ServerLevel) owner.level();
            position = owner.position().toVector3f();
        }
        if (level == null || position == null) return;

        if (!SimpleRadioLibrary.SERVER_CONFIG.frequency.crossDimensional && level != source.origin.level) return;

        this.effect.severity = (float) source.computeSeverity();
        this.effect.volume = source.volume;
        if (this.effect.severity >= 100) return;

        // Packet buffer
        List<short[]> microphonePackets = packetBuffer.computeIfAbsent(source.owner, k -> new ArrayList<>());
        if (microphonePackets.isEmpty()) {
            for (int i = 0; i < SimpleRadioLibrary.SERVER_CONFIG.frequency.packetBuffer; i++) {
                microphonePackets.add(null);
            }
        }

        // Decoding
        byte[] data = source.data;

        OpusDecoder decoder = getDecoder(source.owner);
        if (data == null || data.length == 0) {
            decoder.resetState();
            return;
        }
        short[] decoded = decoder.decode(data);
        microphonePackets.add(decoded);

        // Loader-specific compat
        Services.COMPAT.onData(this, source, decoded);

        // Common compat
        CompatCore.onData(this, source, decoded);

        if (this.audioPlayer == null)
            getAudioPlayer().startPlaying();
    }

    public OpusDecoder getDecoder(UUID sender) {
        return decoders.computeIfAbsent(sender, uuid -> CommonRadioPlugin.serverApi.createDecoder());
    }

    private AudioPlayer getAudioPlayer() {
        if (this.audioPlayer == null) {
            if (this.owner == null) {
                LocationalAudioChannel locationalChannel = CommonRadioPlugin.serverApi.createLocationalAudioChannel(this.id,
                        CommonRadioPlugin.serverApi.fromServerLevel(location.level),
                        CommonRadioPlugin.serverApi.createPosition(location.x + 0.5, location.y + 0.5, location.z + 0.5)
                );
                locationalChannel.setDistance(range);
                locationalChannel.setCategory(CommonRadioPlugin.RADIOS_CATEGORY);

                this.audioChannel = locationalChannel;
            } else {
                this.audioChannel = CommonRadioPlugin.serverApi.createEntityAudioChannel(this.id, (de.maxhenkel.voicechat.api.Entity) this.owner);
                audioChannel.setCategory(CommonRadioPlugin.TRANSCEIVERS_CATEGORY);
            }

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
