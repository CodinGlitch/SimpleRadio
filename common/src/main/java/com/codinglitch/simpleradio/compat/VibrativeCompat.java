package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioMessage;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.vibrativevoice.VibrativeVoiceApi;

public class VibrativeCompat {
    public static void onData(RadioSpeaker speaker, RadioMessage source, short[] decodedData) {
        VibrativeVoiceApi.VibrationType type = VibrativeVoiceApi.INSTANCE.getQualifyingType(decodedData);
        if (type == null) return;

        WorldlyPosition location = speaker.getLocation();
        if (location == null) return;

        VibrativeVoiceApi.INSTANCE.trySendVibration(speaker.reference, location.blockPos(), location.level, type);
    }
}
