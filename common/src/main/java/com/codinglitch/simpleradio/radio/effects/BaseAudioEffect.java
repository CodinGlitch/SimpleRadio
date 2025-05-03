package com.codinglitch.simpleradio.radio.effects;

import java.util.Random;

public class BaseAudioEffect extends AudioEffect {
    public static Random RANDOM = new Random();

    private float lastSample = 0;

    public BaseAudioEffect() {}

    @Override
    public short[] apply(short[] data) {
        for (int i = 0; i < data.length; i++) {
            if (RANDOM.nextFloat(100) < severity) {
                data[i] *= 0;
            }
        }

        bitCrush(data, 12 - Math.round(severity / 15));
        downsample(data, 5 + Math.round(severity / 15));
        lowPass(data);

        return super.apply(data);
    }

    /**
     * Simple single-pole IIR low-pass filter
     */
    public void lowPass(short[] data) {
        float alpha = 0.1f;

        for (int i = 0; i < data.length; i++) {
            float filtered = alpha * (float)data[i] + (1.0f - alpha) * lastSample;

            data[i] = (short) filtered;

            lastSample = filtered;
        }
    }

    public void bitCrush(short[] data, int targetDepth) {
        int factor = 1 << (16 - targetDepth);

        // Process each sample in the array
        for (int i = 0; i < data.length; i++) {
            // Get the current sample
            int sample = data[i];

            // Quantize the sample by rounding to the nearest multiple of factor
            // This reduces the resolution by effectively zeroing out the lower bits
            int quantized = ((sample + (factor / 2)) / factor) * factor;

            // Store the quantized value back in the array
            data[i] = (short)quantized;
        }
    }

    public void downsample(short[] data, int factor) {
        if (factor < 1) return;

        // Process the samples in blocks of 'factor'
        for (int i = 0; i < data.length; i += factor) {
            // Pick the sample from the beginning of the block.
            short sample = data[i];

            // Replicate that sample over the block (without exceeding numSamples)
            for (int j = i; j < i + factor && j < data.length; j++) {
                data[j] = sample;
            }
        }
    }
}
