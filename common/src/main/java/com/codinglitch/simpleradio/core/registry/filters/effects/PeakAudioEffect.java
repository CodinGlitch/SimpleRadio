package com.codinglitch.simpleradio.core.registry.filters.effects;

import com.codinglitch.simpleradio.radio.AudioEffect;

public class PeakAudioEffect implements AudioEffect {

    private final float loAlpha;    // lower edge
    private final float hiAlpha;    // upper edge

    private float bandLo, bandHi;

    private final float strength;

    public PeakAudioEffect(float low, float high, float strength) {
        this.strength = strength;

        loAlpha = AudioEffect.cutoffAlpha(low);
        hiAlpha = AudioEffect.cutoffAlpha(high);
    }

    @Override
    public void process(float[] data) {
        for (int i = 0; i < data.length; i++) {
            float s = data[i];

            // re-add audio from low-high
            bandLo += loAlpha * (s - bandLo);
            bandHi += hiAlpha * (s - bandHi);
            s += strength * (bandHi - bandLo);

            data[i] = s;
        }
    }
}
