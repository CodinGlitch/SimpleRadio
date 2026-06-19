package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.FrequencingType;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.radio.Message;

/**
 * A type of {@link Router} that accepts {@link Message}s from its connected {@link Frequency}.
 * <br>
 * <b>Does route further.</b>
 */
public interface Receiver extends Router {

    int getAntennaPower();
    float getPower();
    FrequencingType getFrequencingType();

    Receiver frequency(Frequency frequency);
    Receiver frequencingType(FrequencingType type);
}
