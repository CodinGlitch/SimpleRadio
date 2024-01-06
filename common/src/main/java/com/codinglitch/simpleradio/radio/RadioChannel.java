package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.StaticPosition;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.codinglitch.simpleradio.radio.effects.BaseAudioEffect;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import de.maxhenkel.voicechat.api.packets.SoundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Supplier;

public class RadioChannel implements Supplier<short[]> {
    public UUID owner;
    public StaticPosition location;
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

    public void transmit(UUID sender, Vec3 senderLocation, byte[] data) {
        List<short[]> microphonePackets = packetBuffer.computeIfAbsent(sender, k -> new ArrayList<>());

        if (microphonePackets.isEmpty()) {
            for (int i = 0; i < 6; i++) { //TODO: move to config when added
                microphonePackets.add(null);
            }
        }

        OpusDecoder decoder = getDecoder(sender);
        if (data == null || data.length <= 0) {
            decoder.resetState();
            return;
        }
        microphonePackets.add(decoder.decode(data));

        this.effect.severity = 5;

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
                        CommonRadioPlugin.serverApi.createPosition(location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5)
                );
                locationalChannel.setDistance(32f);
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
