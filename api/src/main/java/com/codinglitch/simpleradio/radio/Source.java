package com.codinglitch.simpleradio.radio;

import net.minecraft.sounds.SoundEvent;

import java.util.UUID;

/**
 * Contains the audio data for radio transmissions through a {@link Message}. This can be
 * shared across different messages, and encoded data will be decoded on demand.
 * <br>
 * For messages that contain sounds instead of raw data, this will contain their identifiers via string.
 */
public interface Source {

    byte[] getData();
    short[] getDecoded(UUID id);

    SoundEvent getSoundEvent();
    String getSound();
}
