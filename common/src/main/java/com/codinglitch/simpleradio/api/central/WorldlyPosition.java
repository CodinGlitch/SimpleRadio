package com.codinglitch.simpleradio.api.central;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.text.NumberFormat;
import java.util.Locale;

public class WorldlyPosition extends Vector3f {
    public Level level;

    // immutable, set on creation
    private final BlockPos realLocation;

    public WorldlyPosition(float x, float y, float z, Level level, BlockPos realLocation) {
        super(x, y, z);
        this.level = level;
        this.realLocation = realLocation;
    }
    public WorldlyPosition(float x, float y, float z, Level level) {
        this(x, y, z, level, null);
    }
    public WorldlyPosition() {
        this(0, 0, 0, null, null);
    }

    public static WorldlyPosition of(BlockPos pos, Level level, BlockPos realLocation) { // use this upon creation to save the 'real' location
        return new WorldlyPosition(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, level, realLocation);
    }
    public static WorldlyPosition of(BlockPos pos, Level level) {
        return new WorldlyPosition(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, level);
    }
    public static WorldlyPosition of(Vector3f pos, Level level, BlockPos realLocation) { // use this upon creation to save the 'real' location
        return new WorldlyPosition(pos.x(), pos.y(), pos.z(), level, realLocation);
    }
    public static WorldlyPosition of(Vector3f pos, Level level) {
        return new WorldlyPosition(pos.x(), pos.y(), pos.z(), level);
    }

    public Vector3f position() {
        return this;
    }

    public boolean equals(WorldlyPosition location) {
        if (location == null) return false;
        if (location.level == null) return false;
        if (this.level == null) return false;

        return location.level == this.level && location.position() == this.position();
    }

    public BlockPos blockPos() {
        return new BlockPos((int) Math.floor(this.x()), (int) Math.floor(this.y()), (int) Math.floor(this.z()));
    }
    public BlockPos realLocation() { // used in garbage collection pretty much exclusively for VS and maybe Create: Aeronautics when released
        return this.realLocation == null ? this.blockPos() : this.realLocation;
    }

    public Vector3f dimensionScaled() {
        Vector3f scaled = this.position().copy();
        scaled.mul((float) level.dimensionType().coordinateScale());
        return scaled;
    }

    public float distance(WorldlyPosition other) {
        Vector3f vec = this.dimensionScaled();
        vec.sub(other.dimensionScaled());
        return (float) Mth.length(vec.x(), vec.y(), vec.z());
    }
    public float rawDistance(WorldlyPosition other) {
        return (float) Mth.length(this.x() - other.x(), this.y() - other.y(), this.z() - other.z());
    }
    public float rawDistance(float x, float y, float z) {
        return (float) Mth.length(this.x() - x, this.y() - y, this.z() - z);
    }

    public boolean isClientSide() {
        return this.level.isClientSide;
    }

}
