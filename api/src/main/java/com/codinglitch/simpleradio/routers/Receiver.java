package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.FrequencingType;
import com.codinglitch.simpleradio.central.Frequency;

public interface Receiver extends Router {

    int getAntennaPower();
    float getPower();
    FrequencingType getFrequencingType();

    Receiver frequency(Frequency frequency);
    Receiver frequencingType(FrequencingType type);
}
