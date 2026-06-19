package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.FrequencingType;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.radio.Source;

/**
 * A type of {@link Router} that accepts {@link Source}s and transmits them along its connected {@link Frequency}.
 * <br>
 * <b>Does route further.</b>
 */
public interface Transmitter extends Router {

    int getAntennaPower();
    float getPower(Frequency.Modulation modulation);
    FrequencingType getFrequencingType();

    Transmitter frequency(Frequency frequency);
    Transmitter frequencingType(FrequencingType type);
}
