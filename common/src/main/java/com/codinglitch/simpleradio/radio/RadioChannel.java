package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;
import org.lwjgl.openal.EXTEfx;

import java.util.*;
import java.util.function.Supplier;

public class RadioChannel implements Supplier<short[]> {
    public UUID owner;
    public WorldlyPosition location;
    public AudioPlayer audioPlayer;
    private final Map<UUID, List<short[]>> packetBuffer;
    private final Map<UUID, OpusDecoder> decoders;
    private final AudioEffect effect;

    public RadioChannel(Player owner) {
        this(owner.getUUID());
    }
    public RadioChannel(UUID owner) {
        this.owner = owner;

        packetBuffer = new HashMap<>();
        decoders = new HashMap<>();
        effect = new BaseAudioEffect();
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

    public void transmit(RadioSource source, Frequency frequency) {
        // Severity calculation
        ServerLevel level = null;
        Vector3f position = null;
        if (location != null) {
            level = (ServerLevel) location.level;
            position = location.position();
        } else {
            VoicechatConnection connection = CommonRadioPlugin.serverApi.getConnectionOf(owner);
            if (connection != null) {
                ServerPlayer player = (ServerPlayer) connection.getPlayer().getPlayer();
                if (player != null) {
                    level = player.serverLevel();
                    position = player.position().toVector3f();
                }
            }
        }
        if (level == null || position == null) return;

        if (!CommonSimpleRadio.SERVER_CONFIG.frequency.crossDimensional && level != source.location.level) return;

        this.effect.severity = source.computeSeverity(WorldlyPosition.of(position, level), frequency);
        if (this.effect.severity >= 100) return;

        // Packet buffer
        List<short[]> microphonePackets = packetBuffer.computeIfAbsent(source.owner, k -> new ArrayList<>());
        if (microphonePackets.isEmpty()) {
            for (int i = 0; i < CommonSimpleRadio.SERVER_CONFIG.frequency.packetBuffer; i++) {
                microphonePackets.add(null);
            }
        }

        // Decoding
        byte[] data = source.data;

        OpusDecoder decoder = getDecoder(source.owner);
        if (data == null || data.length <= 0) {
            decoder.resetState();
            return;
        }
        microphonePackets.add(decoder.decode(data));

        if (this.audioPlayer == null)
            getAudioPlayer().startPlaying();
    }

    private OpusDecoder getDecoder(UUID sender) {
        return decoders.computeIfAbsent(sender, uuid -> CommonRadioPlugin.serverApi.createDecoder());
    }

    private AudioPlayer getAudioPlayer() {
        if (this.audioPlayer == null) {
            VoicechatConnection connection = CommonRadioPlugin.serverApi.getConnectionOf(owner);
            AudioChannel channel;
            if (connection == null) {
                LocationalAudioChannel locationalChannel = CommonRadioPlugin.serverApi.createLocationalAudioChannel(this.owner,
                        CommonRadioPlugin.serverApi.fromServerLevel(location.level),
                        CommonRadioPlugin.serverApi.createPosition(location.x + 0.5, location.y + 0.5, location.z + 0.5)
                );
                locationalChannel.setDistance(CommonSimpleRadio.SERVER_CONFIG.radio.range);
                locationalChannel.setCategory(CommonRadioPlugin.RADIOS_CATEGORY);

                channel = locationalChannel;
            } else {
                channel = CommonRadioPlugin.serverApi.createEntityAudioChannel(this.owner, connection.getPlayer());
                channel.setCategory(CommonRadioPlugin.TRANSCEIVERS_CATEGORY);
            }

            this.audioPlayer = CommonRadioPlugin.serverApi.createAudioPlayer(channel, CommonRadioPlugin.serverApi.createEncoder(), this);
        }
        return this.audioPlayer;
    }
}
