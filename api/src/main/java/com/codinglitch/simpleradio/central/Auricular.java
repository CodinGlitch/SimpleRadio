package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;

import java.util.UUID;

public interface Auricular {
    static boolean validateLocation(WorldlyPosition position, Class<?> clazz, UUID reference) {
        return ServerSimpleRadioApi.getInstance().verifyLocationCollection(position, clazz);
    }
}
