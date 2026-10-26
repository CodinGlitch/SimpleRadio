package com.codinglitch.simpleradio.core.registry.filters.effects;

import com.codinglitch.simpleradio.radio.AudioEffect;

import java.util.SplittableRandom;

public class NoiseAudioEffect implements AudioEffect {
    private final SplittableRandom rng = new SplittableRandom();

    private float severity = 0.0f;
    public float getSeverity() { return severity; }
    public void setSeverity(float severity) { this.severity = severity; }

    private float level;
    private final float attack;
    private final float release;

    private float hiss, rough;

    float background;
    float distort;

    float follow = 0.10f;   // extra noise added per unit of voice level

    private float nLp;
    private float phase;

    public NoiseAudioEffect(float attack, float release, float background, float distort) {
        this.attack = attack;
        this.release = release;

        this.background = background;
        this.distort = distort;
    }

    @Override
    public void process(float[] data) {
        float alpha = severity * 0.05f;
        for (int i = 0; i < data.length; i++) {
            float sample = data[i];

            // voice level tracking
            float level = Math.abs(sample);
            this.level += (level > this.level ? attack : release) * (level - this.level);

            // two noise sources with different colors
            float n = (float) (rng.nextDouble() * 2.0 - 1.0);
            hiss  += 0.35f * (n - hiss);
            rough += 0.08f * (n - rough);

            float modulation = 0.6f + 0.4f * (float) Math.sin(phase) * (float) Math.cos(phase*2);

            // Voice gets distorted by noise then noise is layered on top scaled by the voice
            float noiseAmp = background + follow * this.level;

            float roughMult = 1f + distort * rough * 4f * alpha;
            float hissMult = noiseAmp * modulation * alpha;

            data[i] = sample*roughMult + hiss*hissMult;

            phase = (float) (phase + 2f * Math.PI * 0.7f / SAMPLE_RATE);
        }
    }
}
