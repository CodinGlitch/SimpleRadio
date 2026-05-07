package com.codinglitch.simpleradio.compat.sable;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class ClientSableCompat {
    @Nullable
    public static WorldlyPosition modifyPosition(WorldlyPosition position) {
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(position.realLocation());
        if (subLevel == null) return null;

        Vec3 center = position.realLocation().getCenter();
        position.set(subLevel.renderPose().transformPosition(new Vector3d(center.x, center.y, center.z)));
        return position;
    }

    @Nullable
    public static Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {
        // Rotation can be null, so we only update it if we have one.
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(position.realLocation());
        if (subLevel == null) return rotation;
        if (rotation == null) return new Quaternionf(subLevel.renderPose().orientation());

        rotation.set(subLevel.renderPose().orientation());
        return rotation;
    }
}
