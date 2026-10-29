package com.codinglitch.simpleradio.compat;

import com.codinglitch.intermediator.Intermediation;
import com.codinglitch.intermediator.Mediation;
import com.codinglitch.intermediator.Subscriber;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.phases.OnDataPhase;
import com.codinglitch.vibrativevoice.VibrativeVoiceApi;

public class VibrativeCompat implements Mediation {

    @Override
    public void mediate(Intermediation intermediation) {
        intermediation.subscribe(OnDataPhase.class, Subscriber.Condition.ENABLED, this::onData);
    }

    public void onData(OnDataPhase data) {
        VibrativeVoiceApi.VibrationType type = VibrativeVoiceApi.INSTANCE.getQualifyingType(data.decodedData);
        if (type == null) return;

        WorldlyPosition location = data.speaker.getLocation();
        if (location == null) return;

        VibrativeVoiceApi.INSTANCE.trySendVibration(data.speaker.reference, location.blockPos(), location.level, type);
    }
}
