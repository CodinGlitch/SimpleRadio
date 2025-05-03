package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.radio.RadioManager;

import java.util.UUID;

public interface Auricular {
    static boolean validateLocation(WorldlyPosition position, Class<?> clazz, UUID reference) {
        return RadioManager.getInstance().verifyLocationCollection(position, clazz);
    }
}
