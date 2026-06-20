package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.filters.BasicAudioEffect;
import com.codinglitch.simpleradio.core.registry.filters.MuffledAudioEffect;
import com.codinglitch.simpleradio.core.registry.filters.RadioAudioEffect;

public class SimpleRadioAudioEffects {
    public static final AudioEffectRegistry.AudioEffectLoader<BasicAudioEffect> BASIC = AudioEffectRegistry.register(
            CommonSimpleRadio.id("basic"), BasicAudioEffect::new
    );

    public static final AudioEffectRegistry.AudioEffectLoader<MuffledAudioEffect> MUFFLED = AudioEffectRegistry.register(
            CommonSimpleRadio.id("muffled"), MuffledAudioEffect::new
    );

    public static final AudioEffectRegistry.AudioEffectLoader<RadioAudioEffect> RADIO = AudioEffectRegistry.register(
            CommonSimpleRadio.id("radio"), RadioAudioEffect::new
    );
}
