package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.SimpleRadioApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Math;
import org.joml.Vector3f;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

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
        return WorldlyPosition.of(pos.getCenter().toVector3f(), level, realLocation);
    }
    public static WorldlyPosition of(BlockPos pos, Level level) {
        return WorldlyPosition.of(pos.getCenter().toVector3f(), level);
    }
    public static WorldlyPosition of(Vector3f pos, Level level, BlockPos realLocation) { // use this upon creation to save the 'real' location
        return new WorldlyPosition(pos.x, pos.y, pos.z, level, realLocation);
    }
    public static WorldlyPosition of(Vector3f pos, Level level) {
        return new WorldlyPosition(pos.x, pos.y, pos.z, level);
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
        return new BlockPos((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z));
    }
    public BlockPos realLocation() { // used in garbage collection pretty much exclusively for VS and maybe Create: Aeronautics when released
        return this.realLocation == null ? this.blockPos() : this.realLocation;
    }

    public Vector3f dimensionScaled() {
        float scale = (float) level.dimensionType().coordinateScale();
        return this.position().mul(scale, 1f, scale, new Vector3f());
    }
    public float distance(WorldlyPosition other) {
        DimensionType ours = level.dimensionType();
        DimensionType theirs = other.level.dimensionType();
        if (ours == theirs) return position().distance(other);

        Vector3f scaled = this.dimensionScaled();
        Vector3f otherScaled = other.dimensionScaled();

        Optional<Double> scaling = SimpleRadioApi.getInstance().getConfig("frequency.dimensionalScaling");
        float forgiveness = scaling.map(
        scale -> (float) (Math.abs(ours.coordinateScale() - theirs.coordinateScale()) * scale)
        ).orElse(1f);

        return scaled.distance(otherScaled) / forgiveness;
    }

    public boolean isClientSide() {
        return this.level.isClientSide;
    }

    public BlockEntity getBlockEntity() {
        return level.getBlockEntity(blockPos());
    }

    public BlockState getBlockState() {
        return level.getBlockState(blockPos());
    }

    @Override
    public String toString() {
        return toString(NumberFormat.getNumberInstance(Locale.ENGLISH));
    }
}
