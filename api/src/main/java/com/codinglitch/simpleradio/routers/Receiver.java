package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.FrequencingType;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.radio.RadioSource;

import java.util.function.Predicate;

public interface Receiver extends Router {

    int getAntennaPower();
    double getPower();
    FrequencingType getFrequencingType();

    Receiver frequency(Frequency frequency);
    Receiver receiveCriteria(Predicate<RadioSource> criteria);
    Receiver frequencingType(FrequencingType type);
}
