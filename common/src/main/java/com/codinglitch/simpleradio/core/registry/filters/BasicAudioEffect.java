package com.codinglitch.simpleradio.core.registry.filters;

import com.codinglitch.simpleradio.radio.AudioEffect;

public class BasicAudioEffect extends AudioEffect {
    public float severity;
    public float volume;

    public short[] apply(short[] data) {
        return data;
    }
}
