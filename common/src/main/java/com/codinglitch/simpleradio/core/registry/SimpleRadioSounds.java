package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SimpleRadioSounds {
    public static final SoundEvent RADIO_OPEN = SoundEvent.createVariableRangeEvent(new ResourceLocation(CommonSimpleRadio.ID, "radio_open"));
    public static final SoundEvent RADIO_CLOSE = SoundEvent.createVariableRangeEvent(new ResourceLocation(CommonSimpleRadio.ID, "radio_close"));

    public static final SoundEvent SHORT_CIRCUIT = SoundEvent.createVariableRangeEvent(new ResourceLocation(CommonSimpleRadio.ID, "short_circuit"));
}
