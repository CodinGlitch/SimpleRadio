package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.FrequencingType;
import com.codinglitch.simpleradio.central.Frequency;

public interface Transmitter extends Router {

    int getAntennaPower();
    float getPower(Frequency.Modulation modulation);
    FrequencingType getFrequencingType();

    Transmitter frequency(Frequency frequency);
    Transmitter frequencingType(FrequencingType type);
}
