package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.ItemHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleRadioSounds {
    public static final Map<ResourceLocation, SoundEvent> SOUNDS = new LinkedHashMap<>();

    public static final SoundEvent RADIO_OPEN = register(new SoundEvent(new ResourceLocation(CommonSimpleRadio.ID, "radio_open")));
    public static final SoundEvent RADIO_CLOSE = register(new SoundEvent(new ResourceLocation(CommonSimpleRadio.ID, "radio_close")));

    public static final SoundEvent SHORT_CIRCUIT = register(new SoundEvent(new ResourceLocation(CommonSimpleRadio.ID, "short_circuit")));

    public static final SoundEvent TILT_MICROPHONE = register(new SoundEvent(new ResourceLocation(CommonSimpleRadio.ID, "tilt_microphone")));
    public static final SoundEvent PRESS_MICROPHONE = register(new SoundEvent(new ResourceLocation(CommonSimpleRadio.ID, "press_microphone")));

    public static final SoundEvent SPIN_INSULATOR = register(new SoundEvent(new ResourceLocation(CommonSimpleRadio.ID, "spin_insulator")));

    public static SoundEvent register(SoundEvent sound) {
        SOUNDS.put(sound.getLocation(), sound);
        return sound;
    }
}
