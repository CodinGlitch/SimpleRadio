package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public interface Antennal {
    BooleanProperty UP = BlockStateProperties.UP;
    BooleanProperty DOWN = BlockStateProperties.DOWN;
    BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    
    Direction[] Z_PRIORITY = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    Direction[] X_PRIORITY = new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};


    int getMaxDistance();
    
    default BlockState update(BlockPos pos, BlockState state, LevelAccessor accessor, @Nullable Direction.Axis priority) {
        for (Direction direction : (priority == Direction.Axis.X ? X_PRIORITY : Z_PRIORITY)) {
            Direction.Axis axis = state.getValue(AXIS);

            if (axis.isVertical() || axis.test(direction)) {
                BlockPos otherPos = pos.relative(direction);

                BlockState otherState = accessor.getBlockState(otherPos);
                BlockState otherSideState = accessor.getBlockState(pos.relative(direction.getOpposite()));


                boolean supporting = false;

                if (otherState.getBlock() instanceof Antennal) {
                    Direction.Axis otherAxis = otherState.getValue(AXIS);
                    supporting = otherAxis == Direction.Axis.Y || otherAxis == direction.getAxis();
                }

                if (otherSideState.getBlock() instanceof Antennal) {
                    Direction.Axis otherAxis = otherSideState.getValue(AXIS);
                    supporting = supporting || otherAxis == Direction.Axis.Y || otherAxis == direction.getAxis();
                }

                if (supporting) {
                    state = state.setValue(AXIS, direction.getAxis());
                } else {
                    state = state.setValue(AXIS, Direction.Axis.Y);
                }
            }
        }

        BlockPos bottomPos = pos.below();
        BlockState bottomState = accessor.getBlockState(bottomPos);

        state = state.setValue(UP, accessor.getBlockState(pos.above()).getBlock() instanceof Antennal)
                .setValue(DOWN, !bottomState.isAir())
                .setValue(ATTACHED, bottomState.isFaceSturdy(accessor, bottomPos, Direction.UP));

        return state;
    }

    default Pair<Integer, Boolean> crawlAntenna(BlockPos pos, BlockState state, LevelAccessor accessor) {
        if (state.getValue(ATTACHED)) return new Pair<>(0, true);

        int columnDistance = -1;
        boolean wasDirect = false;
        if (state.getValue(DOWN)) {
            Pair<Integer, Boolean> result = crawlColumn(pos.mutable().move(Direction.DOWN), accessor, 1);
            columnDistance = result.getA();
            wasDirect = result.getB();
        }

        int axisDistance = -1;
        Direction.Axis axis = state.getValue(AXIS);
        if (!axis.isVertical()) {
            axisDistance = crawlAxis(pos.mutable(), axis, accessor, 0);
        }

        if (columnDistance == -1) return new Pair<>(axisDistance, wasDirect);
        if (axisDistance == -1) return new Pair<>(columnDistance, wasDirect);

        if (columnDistance <= axisDistance) {
            return new Pair<>(columnDistance, wasDirect);
        } else {
            return new Pair<>(axisDistance, wasDirect);
        }
    }

    default int climbAntenna(BlockPos pos, LevelAccessor accessor) {
        AtomicInteger score = new AtomicInteger();
        List<BlockPos> navigated = new ArrayList<>();

        climbColumn(pos.mutable(), accessor, score, 0, navigated);

        return score.get();
    }

    default void notifyExtension(BlockPos pos, LevelAccessor accessor) {
        BlockPos travelledPos = ServerSimpleRadioApi.getInstance().travelExtension(pos, accessor);
        //if (travelledPos == pos) return;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos relativePosition = travelledPos.relative(direction);
            BlockEntity blockEntity = accessor.getBlockEntity(relativePosition);

            if (blockEntity instanceof Frequencing frequencing) {
                frequencing.markDirty();
            }
        }
    }

    // ---- Crawling/Climbing Methods ---- \\

    default void climbAxis(BlockPos.MutableBlockPos currentPos, Direction.Axis axis, LevelAccessor accessor, AtomicInteger score, int distance, List<BlockPos> navigated) {
        Direction positiveDirection = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction negativeDirection = Direction.get(Direction.AxisDirection.NEGATIVE, axis);

        climbRow(currentPos.mutable().move(positiveDirection), positiveDirection, accessor, score, distance+1, navigated);
        climbRow(currentPos.mutable().move(negativeDirection), negativeDirection, accessor, score, distance+1, navigated);
    }
    default int crawlAxis(BlockPos.MutableBlockPos currentPos, Direction.Axis axis, LevelAccessor accessor, int distance) {
        Direction positiveDirection = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction negativeDirection = Direction.get(Direction.AxisDirection.NEGATIVE, axis);

        int positiveDistance = crawlRow(currentPos.mutable().move(positiveDirection), positiveDirection, accessor,  distance+1);
        int negativeDistance = crawlRow(currentPos.mutable().move(negativeDirection), negativeDirection, accessor, distance+1);

        if (positiveDistance == -1) return negativeDistance;
        if (negativeDistance == -1) return positiveDistance;

        return Math.min(positiveDistance, negativeDistance);
    }

    default void climbRow(BlockPos.MutableBlockPos currentPos, Direction direction, LevelAccessor accessor, AtomicInteger score, int distance, List<BlockPos> navigated) {
        AtomicInteger dist = new AtomicInteger(distance);

        this.iterateDirection(currentPos, accessor, direction, state -> {
            if (navigated.stream().anyMatch(nav -> nav.equals(currentPos))) return false;
            if (dist.get() > getMaxDistance()) return false;

            navigated.add(currentPos.immutable());

            if (state.getValue(UP)) {
                climbColumn(currentPos.mutable().move(Direction.UP), accessor,  score, distance+1, navigated);
            }

            score.getAndIncrement();
            dist.getAndIncrement();
            return true;
        });
    }
    default int crawlRow(BlockPos.MutableBlockPos currentPos, Direction direction, LevelAccessor accessor, int distance) {
        ArrayList<Integer> distances = new ArrayList<>();
        AtomicInteger dist = new AtomicInteger(distance);

        this.iterateDirection(currentPos, accessor, direction, state -> {
            if (dist.get() > getMaxDistance()) return false;
            if (state.getValue(ATTACHED)) {
                distances.add(dist.get());
                return false;
            }

            if (state.getValue(DOWN)) {
                int otherDistance = crawlColumn(currentPos.mutable().move(Direction.DOWN), accessor, dist.get()+1).getA();
                if (otherDistance != -1) {
                    distances.add(otherDistance);
                }
            }

            dist.getAndIncrement();
            return true;
        });

        if (distances.isEmpty()) return -1;
        return Collections.min(distances);
    }

    default void climbColumn(BlockPos.MutableBlockPos currentPos, LevelAccessor accessor, AtomicInteger score, int distance, List<BlockPos> navigated) {
        AtomicInteger dist = new AtomicInteger(distance);

        this.iterateDirection(currentPos, accessor, Direction.UP, state -> {
            if (navigated.stream().anyMatch(nav -> nav.equals(currentPos))) return false;
            if (dist.get() > getMaxDistance()) return false;

            navigated.add(currentPos.immutable());

            Direction.Axis axis = state.getValue(AXIS);
            if (!axis.isVertical()) {
                score.addAndGet(2);
                climbAxis(currentPos, axis, accessor, score, distance, navigated);
            }

            if (!state.getValue(UP)) {
                score.addAndGet(2);
                return false;
            }

            score.getAndIncrement();
            dist.getAndIncrement();
            return true;
        });
    }
    default Pair<Integer, Boolean> crawlColumn(BlockPos.MutableBlockPos currentPos, LevelAccessor accessor, int distance) {
        ArrayList<Integer> distances = new ArrayList<>();

        AtomicBoolean isColumn = new AtomicBoolean(false);
        AtomicInteger dist = new AtomicInteger(distance);

        this.iterateDirection(currentPos, accessor, Direction.DOWN, state -> {
            if (dist.get() > getMaxDistance()) return false;
            if (state.getValue(ATTACHED)) {
                this.notifyExtension(currentPos.below(), accessor);

                isColumn.set(true);
                distances.add(dist.get());
                return false;
            }

            Direction.Axis axis = state.getValue(AXIS);
            if (!axis.isVertical()) {
                int otherDistance = crawlAxis(currentPos, axis, accessor, dist.get());
                if (otherDistance != -1) {
                    distances.add(otherDistance);
                }
            }

            if (!state.getValue(DOWN)) return false;

            dist.getAndIncrement();

            return true;
        });

        if (distances.isEmpty()) return new Pair<>(-1, isColumn.get());
        return new Pair<>(Collections.min(distances), isColumn.get());
    }

    default void iterateDirection(BlockPos.MutableBlockPos currentPos, LevelAccessor accessor, Direction direction, Function<BlockState, Boolean> iterator) {
        BlockState currentState = accessor.getBlockState(currentPos);
        while (currentState.getBlock() instanceof Antennal) {
            if (!iterator.apply(currentState)) break;

            currentPos.move(direction);
            currentState = accessor.getBlockState(currentPos);
        }
    }
}
