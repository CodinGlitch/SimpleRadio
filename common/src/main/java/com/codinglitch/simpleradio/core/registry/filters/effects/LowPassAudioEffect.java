package com.codinglitch.simpleradio.core.registry.filters.effects;

import com.codinglitch.simpleradio.radio.AudioEffect;

public class LowPassAudioEffect implements AudioEffect {
    private final float lowPassAlpha;        // high-pass
    private float lpA, lpB;                     // two low-pass stages

    public LowPassAudioEffect(float cutoff) {
        // low pass at the cutoff
        lowPassAlpha = AudioEffect.cutoffAlpha(cutoff);
    }

    @Override
    public void process(float[] data) {
        for (int i = 0; i < data.length; i++) {
            float s = data[i];

            // low-pass, two stages
            lpA += lowPassAlpha * (s - lpA);
            lpB += lowPassAlpha * (lpA - lpB);
            s = lpB;

            data[i] = s;
        }
    }
}
