package com.codinglitch.simpleradio.core.registry.filters.effects;

import com.codinglitch.simpleradio.radio.AudioEffect;

public class DownsampleAudioEffect implements AudioEffect {
    private final int factor;
    private int counter;
    private float held;

    public DownsampleAudioEffect(int factor) {
        this.factor = factor;
    }

    @Override
    public void process(float[] data) {
        for (int i = 0; i < data.length; i++) {
            if (counter == 0) held = data[i];
            data[i] = held;
            counter = (counter + 1) % 4;
        }
    }
}
