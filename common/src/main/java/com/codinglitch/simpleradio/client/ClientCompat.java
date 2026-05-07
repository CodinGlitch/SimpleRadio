package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.sable.ClientSableCompat;
import com.codinglitch.simpleradio.compat.sable.CommonSableCompat;
import com.codinglitch.simpleradio.platform.ClientServices;
import com.codinglitch.simpleradio.platform.Services;
import org.joml.Quaternionf;

public class ClientCompat {
    public static WorldlyPosition modifyPosition(WorldlyPosition position) {

        if (CompatCore.SABLE.enabled) {
            WorldlyPosition newPosition = ClientSableCompat.modifyPosition(position);
            if (newPosition != null) return newPosition;
        }

        return ClientServices.COMPAT.modifyPosition(position);
    }

    public static Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {

        if (CompatCore.SABLE.enabled) {
            Quaternionf newRotation = ClientSableCompat.modifyRotation(position, rotation);
            if (newRotation != null) return newRotation;
        }

        return ClientServices.COMPAT.modifyRotation(position, rotation);
    }
}
