package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.filters.BasicAudioFilter;
import com.codinglitch.simpleradio.core.registry.filters.MuffledAudioFilter;
import com.codinglitch.simpleradio.core.registry.filters.RadioAudioFilter;

public class SimpleRadioAudioEffects {
    public static final AudioEffectRegistry.AudioEffectLoader<BasicAudioFilter> BASIC = AudioEffectRegistry.register(
            CommonSimpleRadio.id("basic"), BasicAudioFilter::new
    );

    public static final AudioEffectRegistry.AudioEffectLoader<MuffledAudioFilter> MUFFLED = AudioEffectRegistry.register(
            CommonSimpleRadio.id("muffled"), MuffledAudioFilter::new
    );

    public static final AudioEffectRegistry.AudioEffectLoader<RadioAudioFilter> RADIO = AudioEffectRegistry.register(
            CommonSimpleRadio.id("radio"), RadioAudioFilter::new
    );
}
