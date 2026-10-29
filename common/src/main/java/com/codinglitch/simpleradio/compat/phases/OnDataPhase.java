package com.codinglitch.simpleradio.compat.phases;

import com.codinglitch.intermediator.Phase;
import com.codinglitch.simpleradio.radio.RadioMessage;
import com.codinglitch.simpleradio.radio.RadioSpeaker;

public class OnDataPhase implements Phase {
    public RadioSpeaker speaker;
    public RadioMessage message;
    public short[] decodedData;

    public OnDataPhase(RadioSpeaker speaker, RadioMessage message, short[] decodedData) {
        this.speaker = speaker;
        this.message = message;
        this.decodedData = decodedData;
    }
}
