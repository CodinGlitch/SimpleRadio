package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Collections;

public class AntennaBlock extends Block {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final BooleanProperty COLUMN = BooleanProperty.create("column");
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0);
    private static final VoxelShape COLUMN_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape HORIZONTAL_X_SHAPE = Block.box(0.0, 4.0, 4.0, 16.0, 12.0, 12.0);
    private static final VoxelShape HORIZONTAL_Z_SHAPE = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 16.0);

    private static final Direction[] Z_PRIORITY = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    private static final Direction[] X_PRIORITY = new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};

    private static final int MAX_DISTANCE = 8;

    public AntennaBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(UP, false)
                .setValue(DOWN, true)
                .setValue(UNSTABLE, false)
                .setValue(ATTACHED, false)
                .setValue(COLUMN, false)
                .setValue(AXIS, Direction.Axis.Y)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        super.createBlockStateDefinition(stateBuilder.add(UP).add(DOWN).add(UNSTABLE).add(ATTACHED).add(COLUMN).add(AXIS));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (state.getValue(UP)) {
            if (state.getValue(AXIS) == Direction.Axis.X) {
                return Shapes.or(COLUMN_SHAPE, HORIZONTAL_X_SHAPE);
            } else if (state.getValue(AXIS) == Direction.Axis.Z) {
                return Shapes.or(COLUMN_SHAPE, HORIZONTAL_Z_SHAPE);
            }

            return COLUMN_SHAPE;
        } else if (state.getValue(DOWN)) {
            if (state.getValue(AXIS) == Direction.Axis.X) {
                return Shapes.or(SHAPE, HORIZONTAL_X_SHAPE);
            } else if (state.getValue(AXIS) == Direction.Axis.Z) {
                return Shapes.or(SHAPE, HORIZONTAL_Z_SHAPE);
            }
        } else {
            if (state.getValue(AXIS) == Direction.Axis.X) {
                return HORIZONTAL_X_SHAPE;
            } else if (state.getValue(AXIS) == Direction.Axis.Z) {
                return HORIZONTAL_Z_SHAPE;
            }
        }

        return COLUMN_SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor accessor, BlockPos pos, BlockPos otherPos) {
        if (!accessor.isClientSide()) {
            accessor.scheduleTick(pos, this, 0);
        }

        BlockState newState = update(pos, state, accessor, null);
        Pair<Integer, Boolean> result = this.surveyGround(pos, newState, accessor);

        if (result.getA() == -1) {
            return state.setValue(ATTACHED, false).setValue(UNSTABLE, true);
        }
        return newState.setValue(COLUMN, result.getB());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);

        state = update(context.getClickedPos(), state, context.getLevel(), context.getClickedFace().getAxis());

        return state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState otherState, boolean b) {
    }

    @Override
    public void destroy(LevelAccessor accessor, BlockPos pos, BlockState state) {

        super.destroy(accessor, pos, state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos) {

        boolean flag = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState adjacentState = reader.getBlockState(pos.relative(direction));
            if (adjacentState.getBlock() instanceof AntennaBlock) {
                Direction.Axis axis = adjacentState.getValue(AXIS);
                if (!axis.isVertical() && direction.getAxis() != axis) return false;

                flag = true;
            }
        }

        if (flag) {
            for (Direction direction : Direction.Plane.VERTICAL) {
                BlockState adjacentState = reader.getBlockState(pos.relative(direction));
                if (adjacentState.getBlock() instanceof AntennaBlock) {
                    Direction.Axis axis = adjacentState.getValue(AXIS);
                    if (axis == state.getValue(AXIS)) return false;
                }
            }

            return true;
        }

        BlockPos bottomPos = pos.below();
        BlockState bottomState = reader.getBlockState(bottomPos);
        return bottomState.getBlock() instanceof AntennaBlock || bottomState.isFaceSturdy(reader, bottomPos, Direction.UP);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (state.getValue(UNSTABLE)) {
            BlockState belowState = level.getBlockState(pos.below());
            if (!belowState.isAir()) {
                Block block = state.getBlock();
                ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(block));
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);

                level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
            } else {
                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, pos, state.setValue(UNSTABLE, false));
            }
        }
    }

    public BlockState update(BlockPos pos, BlockState state, LevelAccessor accessor, @Nullable Direction.Axis priority) {
        for (Direction direction : (priority == Direction.Axis.X ? X_PRIORITY : Z_PRIORITY)) {
            Direction.Axis axis = state.getValue(AXIS);

            if (axis.isVertical() || axis.test(direction)) {
                BlockPos otherPos = pos.relative(direction);

                BlockState otherState = accessor.getBlockState(otherPos);
                BlockState otherSideState = accessor.getBlockState(pos.relative(direction.getOpposite()));


                boolean supporting = false;

                if (otherState.getBlock() instanceof AntennaBlock) {
                    Direction.Axis otherAxis = otherState.getValue(AXIS);
                    supporting = otherAxis == Direction.Axis.Y || otherAxis == direction.getAxis();
                }

                if (otherSideState.getBlock() instanceof AntennaBlock) {
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

        state = state.setValue(UP, accessor.getBlockState(pos.above()).getBlock() instanceof AntennaBlock)
            .setValue(DOWN, !bottomState.isAir())
            .setValue(ATTACHED, bottomState.isFaceSturdy(accessor, bottomPos, Direction.UP));

        return state;
    }

    public Pair<Integer, Boolean> surveyGround(BlockPos pos, BlockState state, LevelAccessor accessor) {
        if (state.getValue(ATTACHED)) return new Pair<>(0, true);

        int columnDistance = -1;
        boolean wasDirect = false;
        if (state.getValue(DOWN)) {
            Pair<Integer, Boolean> result = checkColumn(pos.mutable().move(Direction.DOWN), accessor, 1);
            columnDistance = result.getA();
            wasDirect = result.getB();
        }

        Integer axisDistance = -1;
        Direction.Axis axis = state.getValue(AXIS);
        if (!axis.isVertical()) {
            axisDistance = checkAxis(pos.mutable(), axis, accessor, 0);
        }

        if (columnDistance == -1) return new Pair<>(axisDistance, wasDirect);
        if (axisDistance == -1) return new Pair<>(columnDistance, wasDirect);

        if (columnDistance <= axisDistance) {
            return new Pair<>(columnDistance, wasDirect);
        } else {
            return new Pair<>(axisDistance, wasDirect);
        }
    }

    public int checkAxis(BlockPos.MutableBlockPos currentPos, Direction.Axis axis, LevelAccessor accessor, int distance) {
        Direction positiveDirection = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction negativeDirection = Direction.get(Direction.AxisDirection.NEGATIVE, axis);

        int positiveDistance = checkRow(currentPos.mutable().move(positiveDirection), positiveDirection, accessor, distance+1);
        int negativeDistance = checkRow(currentPos.mutable().move(negativeDirection), negativeDirection, accessor, distance+1);

        if (positiveDistance == -1) return negativeDistance;
        if (negativeDistance == -1) return positiveDistance;

        return Math.min(positiveDistance, negativeDistance);
    }

    public int checkRow(BlockPos.MutableBlockPos currentPos, Direction direction, LevelAccessor accessor, int distance) {
        ArrayList<Integer> distances = new ArrayList<>();

        BlockState currentState = accessor.getBlockState(currentPos);
        while (currentState.getBlock() instanceof AntennaBlock) {
            if (distance > MAX_DISTANCE) break;
            if (currentState.getValue(ATTACHED)) {
                distances.add(distance);
                break;
            }

            if (currentState.getValue(DOWN)) {
                int otherDistance = checkColumn(currentPos.mutable().move(Direction.DOWN), accessor, distance+1).getA();
                if (otherDistance != -1) {
                    distances.add(otherDistance);
                }
            }

            currentPos.move(direction);
            currentState = accessor.getBlockState(currentPos);

            distance++;
        }

        if (distances.isEmpty()) return -1;
        return Collections.min(distances);
    }

    public Pair<Integer, Boolean> checkColumn(BlockPos.MutableBlockPos currentPos, LevelAccessor accessor, int distance) {
        ArrayList<Integer> distances = new ArrayList<>();

        boolean isColumn = false;

        BlockState currentState = accessor.getBlockState(currentPos);
        while (currentState.getBlock() instanceof AntennaBlock) {
            if (distance > MAX_DISTANCE) break;
            if (currentState.getValue(ATTACHED)) {
                isColumn = true;
                distances.add(distance);
                break;
            }

            Direction.Axis axis = currentState.getValue(AXIS);
            if (!axis.isVertical()) {
                int otherDistance = checkAxis(currentPos, axis, accessor, distance);
                if (otherDistance != -1) {
                    distances.add(otherDistance);
                }
            }

            if (!currentState.getValue(DOWN)) break;

            currentPos.move(Direction.DOWN);
            currentState = accessor.getBlockState(currentPos);

            distance++;
        }

        if (distances.isEmpty()) return new Pair<>(-1, isColumn);
        return new Pair<>(Collections.min(distances), isColumn);
    }
}
