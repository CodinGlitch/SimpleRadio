package com.codinglitch.simpleradio.core.registry.filters.effects;

import com.codinglitch.simpleradio.radio.AudioEffect;

import java.util.SplittableRandom;

public class RangeAudioEffect implements AudioEffect {
    private final SplittableRandom rng = new SplittableRandom();

    private float severity = 0.0f;
    public float getSeverity() { return severity; }
    public void setSeverity(float severity) { this.severity = severity; }

    public RangeAudioEffect() {
    }

    @Override
    public void process(float[] data) {

        if (severity < 80f) return;

        int chance = 1 + (int) (1000 * (100f-severity) * 0.05f);

        float alpha = severity * 0.05f;
        for (int i = 0; i < data.length; i++) {
            float sample = data[i];

            int num = rng.nextInt(chance);
            data[i] = sample * (num < 10 ? 0f : 1f);
        }
    }
}
