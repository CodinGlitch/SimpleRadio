package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import net.minecraft.server.level.ServerLevel;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class ValkyrienCompat {
    public static WorldlyPosition modifyPosition(WorldlyPosition position) {
        return CommonValkyrienCompat.modifyPosition(
                VSGameUtilsKt.getShipObjectManagingPos(position.level, position.realLocation()), position
        );
    }
}
