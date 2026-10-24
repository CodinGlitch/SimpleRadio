package com.codinglitch.simpleradio.radio;

/**
 * Chainable audio effects that modify the audio in-place, uses floats instead of shorts
 */
public interface AudioEffect {
    double SAMPLE_RATE = 48000;
    double DT = 1.0f / SAMPLE_RATE;

    static float cutoffAlpha(double cutoff) {
        double rc = 1.0 / (2 * Math.PI * cutoff);
        return (float) (DT / (rc + DT));
    }

    void process(float[] data);
}
