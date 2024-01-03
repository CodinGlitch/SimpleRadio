package com.codinglitch.simpleradio.radio.effects;

public abstract class AudioEffect {
    public float severity;

    public abstract short[] apply(short[] data);
}
