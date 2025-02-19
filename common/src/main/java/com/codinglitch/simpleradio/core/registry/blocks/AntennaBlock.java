package com.codinglitch.simpleradio.core.registry.blocks;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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
        Pair<Integer, Boolean> result = this.crawlAntenna(pos, newState, accessor);

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

    public Pair<Integer, Boolean> crawlAntenna(BlockPos pos, BlockState state, LevelAccessor accessor) {
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

    public int climbAntenna(BlockPos pos, LevelAccessor accessor) {
        AtomicInteger score = new AtomicInteger();
        climbColumn(pos.mutable(), accessor, score, 0);

        return score.get();
    }

    // ---- Crawling/Climbing Methods ---- \\

    public void climbAxis(BlockPos.MutableBlockPos currentPos, Direction.Axis axis, LevelAccessor accessor, AtomicInteger score, int distance) {
        Direction positiveDirection = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction negativeDirection = Direction.get(Direction.AxisDirection.NEGATIVE, axis);

        climbRow(currentPos.mutable().move(positiveDirection), positiveDirection, accessor, score, distance+1);
        climbRow(currentPos.mutable().move(negativeDirection), negativeDirection, accessor, score, distance+1);
    }
    public int crawlAxis(BlockPos.MutableBlockPos currentPos, Direction.Axis axis, LevelAccessor accessor, int distance) {
        Direction positiveDirection = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Direction negativeDirection = Direction.get(Direction.AxisDirection.NEGATIVE, axis);

        int positiveDistance = crawlRow(currentPos.mutable().move(positiveDirection), positiveDirection, accessor,  distance+1);
        int negativeDistance = crawlRow(currentPos.mutable().move(negativeDirection), negativeDirection, accessor, distance+1);

        if (positiveDistance == -1) return negativeDistance;
        if (negativeDistance == -1) return positiveDistance;

        return Math.min(positiveDistance, negativeDistance);
    }

    public void climbRow(BlockPos.MutableBlockPos currentPos, Direction direction, LevelAccessor accessor, AtomicInteger score, int distance) {
        AtomicInteger dist = new AtomicInteger(distance);

        this.iterateDirection(currentPos, accessor, direction, state -> {
            if (dist.get() > MAX_DISTANCE) return false;
            if (state.getValue(UP)) {
                climbColumn(currentPos.mutable().move(Direction.UP), accessor,  score, distance+1);
            }

            score.getAndIncrement();
            dist.getAndIncrement();
            return true;
        });
    }
    public int crawlRow(BlockPos.MutableBlockPos currentPos, Direction direction, LevelAccessor accessor, int distance) {
        ArrayList<Integer> distances = new ArrayList<>();
        AtomicInteger dist = new AtomicInteger(distance);

        this.iterateDirection(currentPos, accessor, direction, state -> {
            if (dist.get() > MAX_DISTANCE) return false;
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

    public void climbColumn(BlockPos.MutableBlockPos currentPos, LevelAccessor accessor, AtomicInteger score, int distance) {
        AtomicInteger dist = new AtomicInteger(distance);

        this.iterateDirection(currentPos, accessor, Direction.UP, state -> {
            if (dist.get() > MAX_DISTANCE) return false;

            Direction.Axis axis = state.getValue(AXIS);
            if (!axis.isVertical()) {
                score.addAndGet(2);
                climbAxis(currentPos, axis, accessor, score, distance);
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
    public Pair<Integer, Boolean> crawlColumn(BlockPos.MutableBlockPos currentPos, LevelAccessor accessor, int distance) {
        ArrayList<Integer> distances = new ArrayList<>();

        AtomicBoolean isColumn = new AtomicBoolean(false);
        AtomicInteger dist = new AtomicInteger(distance);

        this.iterateDirection(currentPos, accessor, Direction.DOWN, state -> {
            if (dist.get() > MAX_DISTANCE) return false;
            if (state.getValue(ATTACHED)) {
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

    public void iterateDirection(BlockPos.MutableBlockPos currentPos, LevelAccessor accessor, Direction direction, Function<BlockState, Boolean> iterator) {
        BlockState currentState = accessor.getBlockState(currentPos);
        while (currentState.getBlock() instanceof AntennaBlock) {
            if (!iterator.apply(currentState)) break;

            currentPos.move(direction);
            currentState = accessor.getBlockState(currentPos);
        }
    }
}
