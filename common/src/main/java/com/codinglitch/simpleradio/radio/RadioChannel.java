package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;

import java.util.*;
import java.util.function.Supplier;

public class RadioChannel implements Supplier<short[]> {
    public UUID owner;
    public AudioPlayer audioPlayer;
    private final Map<UUID, List<short[]>> packetBuffer;
    private final Map<UUID, OpusDecoder> decoders;
    private final AudioEffect effect;

    public RadioChannel(Player owner) {
        this.owner = owner.getUUID();

        packetBuffer = new HashMap<>();
        decoders = new HashMap<>();
        effect = new BaseAudioEffect();
    }

    @Override
    public short[] get() {
        short[] audio = generatePacket();
        if (audio == null) {
            if (audioPlayer != null) {
                audioPlayer.stopPlaying();
            }
            audioPlayer = null;
            return null;
        }
        return audio;
    }

    public short[] generatePacket() {
        List<short[]> packetsToCombine = new ArrayList<>();
        for (Map.Entry<UUID, List<short[]>> packets : packetBuffer.entrySet()) {
            if (packets.getValue().isEmpty()) {
                continue;
            }
            short[] audio = packets.getValue().remove(0);
            packetsToCombine.add(audio);
        }
        packetBuffer.values().removeIf(List::isEmpty);

        if (packetsToCombine.isEmpty()) {
            return null;
        }

        short[] combinedAudio = CommonRadioPlugin.combineAudio(packetsToCombine);

        return effect.apply(combinedAudio);
    }

    public void addPacket(UUID sender, Vec3 senderLocation, byte[] opusEncodedData) {
        List<short[]> microphonePackets = packetBuffer.computeIfAbsent(sender, k -> new ArrayList<>());

        if (microphonePackets.isEmpty()) {
            for (int i = 0; i < 6; i++) { //TODO: move to config when added
                microphonePackets.add(null);
            }
        }

        OpusDecoder decoder = getDecoder(sender);
        if (opusEncodedData == null || opusEncodedData.length <= 0) {
            decoder.resetState();
            return;
        }
        microphonePackets.add(decoder.decode(opusEncodedData));

        effect.severity = 5;

        if (audioPlayer == null) {
            getAudioPlayer().startPlaying();
        }
    }

    private OpusDecoder getDecoder(UUID sender) {
        return decoders.computeIfAbsent(sender, uuid -> CommonRadioPlugin.serverApi.createDecoder());
    }

    private AudioPlayer getAudioPlayer() {
        if (audioPlayer == null) {
            VoicechatConnection connection = CommonRadioPlugin.serverApi.getConnectionOf(owner);
            EntityAudioChannel channel = CommonRadioPlugin.serverApi.createEntityAudioChannel(this.owner, connection.getPlayer());
            audioPlayer = CommonRadioPlugin.serverApi.createAudioPlayer(channel, CommonRadioPlugin.serverApi.createEncoder(), this);
        }
        return audioPlayer;
    }
}
