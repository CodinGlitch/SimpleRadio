package com.codinglitch.simpleradio.core.registry.filters;

import com.codinglitch.simpleradio.radio.AudioFilter;

public class BasicAudioFilter extends AudioFilter {
    public float severity;
    public float volume;

    public short[] apply(short[] data) {
        return data;
    }
}
