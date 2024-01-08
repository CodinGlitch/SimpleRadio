package com.codinglitch.simpleradio.core.central;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class WorldlyPosition extends Vector3f {
    public Level level;

    public WorldlyPosition(float x, float y, float z, Level level) {
        super(x, y, z);
        this.level = level;
    }
    public WorldlyPosition() {}

    public static WorldlyPosition of(BlockPos pos, Level level) {
        return new WorldlyPosition(pos.getX(), pos.getY(), pos.getZ(), level);
    }
    public static WorldlyPosition of(Vector3f pos, Level level) {
        return new WorldlyPosition(pos.x, pos.y, pos.z, level);
    }

    public Vector3f position() {
        return this;
    }

    public Vector3f dimensionScaled() {
        return this.position().mul((float) level.dimensionType().coordinateScale());
    }

    public float distance(WorldlyPosition other) {
        return this.dimensionScaled().distance(other.dimensionScaled());
    }
}
