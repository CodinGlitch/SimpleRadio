package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Supplier;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioSource}s and emits them in-world.
 * <br>
 * Often serves as the end of the audio pipeline.
 * <br>
 * <b>Does not route further.</b>
 */
public class RadioSpeaker extends RadioRouter implements Supplier<short[]> {

    public AudioChannel audioChannel;
    public AudioPlayer audioPlayer;
    private final Map<UUID, Map<UUID, Queue<short[]>>> packetBuffer;
    private final Map<UUID, OpusDecoder> decoders;
    private final AudioEffect effect;

    public float range = 8;

    protected RadioSpeaker(UUID id) {
        super(id);

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

        RadioManager.registerRouterSided(this, owner.level().isClientSide(), null);
    }
    public RadioSpeaker(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioSpeaker(WorldlyPosition location, UUID uuid) {
        this(uuid);
        this.location = location;

        RadioManager.registerRouterSided(this, location.isClientSide(), null);
    }

    public void setRange(float range) {
        this.range = range;
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
        if (source instanceof RadioHeader) return;

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

        // Parsing sound event
        if (source.data == null) {
            if (source.soundEvent == null) return;

            for (ServerPlayer player : level.players()) {
                if (player.position().distanceTo(new Vec3(position)) < 50) {
                    player.connection.send(new ClientboundSoundPacket(
                            Holder.direct(source.soundEvent),
                            SoundSource.BLOCKS,
                            position.x, position.y, position.z,
                            source.volume, source.pitch, level.getLevel().getRandom().nextLong()
                    ));
                }
            }

            return;
        }

        // Packet buffer
        Map<UUID, Queue<short[]>> listenerPackets = packetBuffer.computeIfAbsent(source.owner, k -> new HashMap<>());
        Queue<short[]> playerPackets = listenerPackets.computeIfAbsent(source.originalOwner, k -> new LinkedList<>());
        if (playerPackets.isEmpty()) {
            for (int i = 0; i < SimpleRadioLibrary.SERVER_CONFIG.frequency.packetBuffer; i++) {
                //playerPackets.offer(null);
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

        // Calculate the new length of the resampled data
        int newLength = (int) (decoded.length / source.pitch);
        short[] resampledData = new short[newLength];

        // Perform linear interpolation for resampling
        for (int i = 0; i < newLength; i++) {
            // Calculate the exact position in the original data
            double originalIndex = i * source.pitch;

            // Find the surrounding indices
            int index1 = (int) Math.floor(originalIndex);
            int index2 = Math.min(index1 + 1, decoded.length - 1); // Clamp to avoid out-of-bounds

            // Interpolate between the two points
            double weight2 = originalIndex - index1; // Fractional part
            double weight1 = 1.0 - weight2;

            resampledData[i] = (short) ((decoded[index1] * weight1) + (decoded[index2] * weight2));
        }

        CommonSimpleRadio.info(resampledData);
        playerPackets.offer(effect.apply(resampledData));

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
            if (this.location != null) {
                LocationalAudioChannel locationalChannel = CommonRadioPlugin.serverApi.createLocationalAudioChannel(this.id,
                        CommonRadioPlugin.serverApi.fromServerLevel(location.level),
                        CommonRadioPlugin.serverApi.createPosition(location.x + 0.5, location.y + 0.5, location.z + 0.5)
                );
                locationalChannel.setDistance(range);
                locationalChannel.setCategory(CommonRadioPlugin.RADIOS_CATEGORY);

                this.audioChannel = locationalChannel;
            } else {
                this.audioChannel = CommonRadioPlugin.serverApi.createEntityAudioChannel(
                        this.id,
                        CommonRadioPlugin.serverApi.fromEntity(this.owner)
                );
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
