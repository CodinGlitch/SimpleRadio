package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;
import com.codinglitch.vibrativevoice.VibrativeVoiceApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class VibrativeCompat {
    public static void onData(RadioSpeaker channel, RadioSource source, short[] decodedData) {
        UUID sourceOwner = source.getRealOwner();
        VoicechatConnection connection = CommonRadioPlugin.serverApi.getConnectionOf(sourceOwner);

        VibrativeVoiceApi.VibrationType type = VibrativeVoiceApi.INSTANCE.getQualifyingType(decodedData);

        if (type == null) return;

        if (connection == null) {
            if (channel.location == null) return;

            WorldlyPosition location = channel.location;
            //VibrativeVoiceApi.INSTANCE.trySendVibration(channel.owner, location.blockPos(), location.level, type);
        } else {
            if (connection.getPlayer().getPlayer() instanceof ServerPlayer player) {
                VibrativeVoiceApi.INSTANCE.trySendVibration(player, player.level(), type);
            }
        }
    }
}
