package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class CommonValkyrienCompat {
    public static WorldlyPosition modifyPosition(WorldlyPosition position) {
        Ship ship = VSGameUtilsKt.getLoadedShipManagingPos(position.level, position.realLocation());
        if (ship != null) {
            BlockPos realLocation = position.realLocation();

            Matrix4dc shipToWorld = ship.getTransform().getShipToWorld();
            Vector3d blockPosVec = new Vector3d(realLocation.getX() + 0.5D, realLocation.getY() + 0.5D, realLocation.getZ() + 0.5D);
            Vector3d blockOnShip = shipToWorld.transformPosition(blockPosVec);

            position.x = (float) blockOnShip.x;
            position.y = (float) blockOnShip.y;
            position.z = (float) blockOnShip.z;
        }


        return position;
    }

    public static Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {
        Ship ship = VSGameUtilsKt.getLoadedShipManagingPos(position.level, position.realLocation());
        if (ship != null) {
            Matrix4dc shipToWorld = ship.getTransform().getShipToWorld();
            rotation = new Quaternionf(shipToWorld.getNormalizedRotation(new Quaternionf()));
        }

        return rotation;
    }

    /*public static Vector3f modifyPosition(Ship ship, BlockPos originalBlockPos) {
        if (ship != null) {
            Vector3f pos;

            Matrix4dc shipToWorld = ship.getTransform().getShipToWorld();
            Vector3d blockPosVec = new Vector3d(originalBlockPos.getX() + 0.5D, originalBlockPos.getY() + 0.5D, originalBlockPos.getZ() + 0.5D);
            Vector3d blockOnShip = shipToWorld.transformPosition(blockPosVec);

            pos = new Vector3f((float) blockOnShip.x, (float) blockOnShip.y, (float) blockOnShip.z);

            return pos;
        } else {
            return new Vector3f(originalBlockPos.getX() + 0.5F, originalBlockPos.getY() + 0.5F, originalBlockPos.getZ() + 0.5F);
        }
    }*/
}
