package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import org.joml.Quaternionf;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class ValkyrienCompat {
    public static WorldlyPosition modifyPosition(WorldlyPosition position) {
        return CommonValkyrienCompat.modifyPosition(
                VSGameUtilsKt.getShipObjectManagingPos(position.level, position.realLocation()), position
        );
    }
    public static Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {
        return CommonValkyrienCompat.modifyRotation(
                VSGameUtilsKt.getShipObjectManagingPos(position.level, position.realLocation()), rotation
        );
    }
}
