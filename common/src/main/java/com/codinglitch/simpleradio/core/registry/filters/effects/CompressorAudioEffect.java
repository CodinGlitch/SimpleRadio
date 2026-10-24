package com.codinglitch.simpleradio.core.registry.filters.effects;

import com.codinglitch.simpleradio.radio.AudioEffect;

public class CompressorAudioEffect implements AudioEffect {
    private final float threshold;     // linear level, e.g. 0.1 (about -20 dBFS)
    private final float ratio;         // e.g. 8 means 8:1
    private final float attackCoeff;   // fast: reacts quickly to loud peaks
    private final float releaseCoeff;  // slower: recovers gradually
    private final float makeup;        // linear makeup gain

    private float env;                 // envelope state, persists across frames

    public CompressorAudioEffect(float thresholdDb, float ratio,
                                 float attackMs, float releaseMs, float makeupDb) {
        this.threshold = dbToLin(thresholdDb);
        this.ratio = ratio;
        this.attackCoeff = timeToCoeff(attackMs);
        this.releaseCoeff = timeToCoeff(releaseMs);
        this.makeup = dbToLin(makeupDb);
    }

    // Same idea as alphaFor(): a time constant turned into a per-sample coefficient
    private static float timeToCoeff(float ms) {
        return (float) (1.0 - Math.exp(-1.0 / (ms * 0.001 * SAMPLE_RATE)));
    }

    private static float dbToLin(float db) {
        return (float) Math.pow(10.0, db / 20.0);
    }

    public void process(float[] data) {
        for (int i = 0; i < data.length; i++) {
            float x = data[i];
            float level = Math.abs(x);

            // Envelope follower: fast up, slow down
            float coeff = level > env ? attackCoeff : releaseCoeff;
            env += coeff * (level - env);

            // Gain computer
            float gain = 1f;
            if (env > threshold) {
                // Above threshold, output level rises only 1/ratio as fast as input
                float target = threshold * (float) Math.pow(env / threshold, 1f / 20f);
                gain = target / env;
            }

            data[i] = x * gain * makeup;
        }
    }
}
