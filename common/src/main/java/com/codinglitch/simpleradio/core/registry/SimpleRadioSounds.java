package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import net.minecraft.sounds.SoundEvent;

public class SimpleRadioSounds {
    public static final SoundEvent RADIO_OPEN = SoundEvent.createVariableRangeEvent(CommonSimpleRadio.id("radio_open"));
    public static final SoundEvent RADIO_CLOSE = SoundEvent.createVariableRangeEvent(CommonSimpleRadio.id("radio_close"));

    public static final SoundEvent SHORT_CIRCUIT = SoundEvent.createVariableRangeEvent(CommonSimpleRadio.id("short_circuit"));

    public static final SoundEvent TILT_MICROPHONE = SoundEvent.createVariableRangeEvent(CommonSimpleRadio.id("tilt_microphone"));
    public static final SoundEvent PRESS_MICROPHONE = SoundEvent.createVariableRangeEvent(CommonSimpleRadio.id("press_microphone"));

    public static final SoundEvent SPIN_INSULATOR = SoundEvent.createVariableRangeEvent(CommonSimpleRadio.id( "spin_insulator"));
}
