package com.codinglitch.simpleradio.compat.sable;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3d;


public class CommonSableCompat {

    @Nullable
    public static WorldlyPosition modifyPosition(WorldlyPosition position) {
        if (!SableCompanion.INSTANCE.isInPlotGrid(position.level, position.realLocation())) return null;

        SableCompanion.INSTANCE.getContaining(position.level, position.realLocation()).lastPose();

        Vec3 pos = position.realLocation().getCenter();
        position.set(SableCompanion.INSTANCE.projectOutOfSubLevel(position.level, new Vector3d(pos.x, pos.y, pos.z)));
        return position;
    }

    @Nullable
    public static Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {
        // Rotation can be null, so we only update it if we have one.
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(position.level, position.realLocation());
        if (subLevel == null) return rotation;
        if (rotation == null) return new Quaternionf(subLevel.logicalPose().orientation());

        rotation.set(subLevel.logicalPose().orientation());
        return rotation;
    }
}
