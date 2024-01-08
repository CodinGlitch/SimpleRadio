package com.codinglitch.simpleradio.radio.effects;

import java.util.Random;

public class BaseAudioEffect extends AudioEffect {
    public static Random RANDOM = new Random();

    private static final double SAMPLE_RATE = 48000D;
    private static final double MAX_SHORT = Short.MAX_VALUE;

    private final double normalizedCenterFrequency;
    private final double normalizedBandwidth;

    // State variables for the filter
    private double lastInputSample1;
    private double lastInputSample2;
    private double lastOutputSample1;
    private double lastOutputSample2;

    /**
     * @param centerFrequency center frequency in Hz
     * @param bandwidth       bandwidth in Hz
     */
    public BaseAudioEffect(double centerFrequency, double bandwidth) {
        this.normalizedCenterFrequency = 2D * centerFrequency / SAMPLE_RATE;
        this.normalizedBandwidth = 2D * bandwidth / SAMPLE_RATE;
    }

    public BaseAudioEffect() {
        this(750, 4000);
    }

    @Override
    public short[] apply(short[] data) {
        for (int i = 0; i < data.length; i++) {
            if (RANDOM.nextFloat(100) < severity) data[i] *= 0;
        }

        double[] doubleSamples = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            doubleSamples[i] = data[i] / MAX_SHORT;
        }

        // Apply the bandpass filter
        double maxValue = MAX_SHORT;
        for (int i = 0; i < doubleSamples.length; i++) {
            doubleSamples[i] = bandpassFilter(doubleSamples[i]) * MAX_SHORT;
            if (Math.abs(doubleSamples[i]) > maxValue) {
                maxValue = Math.abs(doubleSamples[i]);
            }
        }

        short[] output = new short[data.length];
        double factor = MAX_SHORT / maxValue;
        for (int i = 0; i < doubleSamples.length; i++) {
            output[i] = (short) (Math.floor(doubleSamples[i] * factor));
        }

        return output;
    }

    /**
     * Bandpass filter implementation (basic second-order IIR filter)
     *
     * @param inputSample input sample
     * @return filtered sample
     */
    private double bandpassFilter(double inputSample) {
        double bandwidth = normalizedBandwidth * (1 - severity * 0.001);

        double w0 = 2D * Math.PI * normalizedCenterFrequency;
        double alpha = Math.sin(w0) * Math.sinh(Math.log(2D) / 2D * bandwidth * w0 / Math.sin(w0));
        double a0 = 1D + alpha;

        double b0 = (1D - Math.cos(w0)) / 2D;
        double b1 = 1D - Math.cos(w0);
        double b2 = b0;
        double a1 = -2D * Math.cos(w0);
        double a2 = 1D - alpha;

        // Apply the bandpass filter
        double filteredSample = (b0 * inputSample + b1 * lastInputSample1 + b2 * lastInputSample2 - a1 * lastOutputSample1 - a2 * lastOutputSample2) / a0;

        // Update the state variables for the next iteration
        lastInputSample2 = lastInputSample1;
        lastInputSample1 = inputSample;
        lastOutputSample2 = lastOutputSample1;
        lastOutputSample1 = filteredSample;

        return filteredSample;
    }
}
