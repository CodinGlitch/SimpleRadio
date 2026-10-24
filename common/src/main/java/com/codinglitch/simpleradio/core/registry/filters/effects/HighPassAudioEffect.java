package com.codinglitch.simpleradio.core.registry.filters.effects;

import com.codinglitch.simpleradio.radio.AudioEffect;

public class HighPassAudioEffect implements AudioEffect {
    private final float highPassAlpha;        // high-pass
    private float hpX1a, hpY1a, hpX1b, hpY1b;   // two high-pass stages

    public HighPassAudioEffect(float cutoff) {
        // high pass at the cutoff
        highPassAlpha = 1 - AudioEffect.cutoffAlpha(cutoff);
    }

    @Override
    public void process(float[] data) {
        for (int i = 0; i < data.length; i++) {
            float s = data[i];

            // high-pass, two stages
            float y = highPassAlpha * (hpY1a + s - hpX1a);
            hpX1a = s; hpY1a = y; s = y;

            y = highPassAlpha * (hpY1b + s - hpX1b);
            hpX1b = s; hpY1b = y; s = y;

            data[i] = s;
        }
    }
}
