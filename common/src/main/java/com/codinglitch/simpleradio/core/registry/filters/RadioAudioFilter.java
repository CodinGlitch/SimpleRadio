package com.codinglitch.simpleradio.core.registry.filters;

import com.codinglitch.simpleradio.core.registry.filters.effects.*;
import com.codinglitch.simpleradio.radio.AudioFilter;

import static com.codinglitch.simpleradio.SimpleRadioLibrary.SERVER_CONFIG;

public class RadioAudioFilter extends AudioFilter {
    private final Chain effects = new Chain(new float[960],
            new NoiseAudioEffect(0.2f, 0.002f, SERVER_CONFIG.audio.noise.background, SERVER_CONFIG.audio.noise.distort),
            new HighPassAudioEffect(SERVER_CONFIG.audio.highPass.cutoff),
            new PeakAudioEffect(SERVER_CONFIG.audio.peak.low, SERVER_CONFIG.audio.peak.high, SERVER_CONFIG.audio.peak.strength),
            new LowPassAudioEffect(SERVER_CONFIG.audio.lowPass.cutoff),
            new DownsampleAudioEffect(SERVER_CONFIG.audio.downsample.factor),
            new CompressorAudioEffect(-24f, 8f, 3f, 80f, 10f)
    );

    public RadioAudioFilter() {}

    @Override
    public void setSeverity(float severity) {
        super.setSeverity(severity);

        // updating effect severity
        effects.get(NoiseAudioEffect.class).setSeverity(severity);
    }

    @Override
    public short[] apply(short[] data) {
        return super.apply(effects.process(data));
    }
}
