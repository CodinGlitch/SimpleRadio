package com.codinglitch.simpleradio.radio.effects;

public abstract class AudioEffect {
    public float severity;
    public float volume;

    public short[] apply(short[] data) {
        for (int i = 0; i < data.length; i++) {
            short audio = data[i];
            data[i] = (short) (audio * volume);
        }

        return data;
    }
}
