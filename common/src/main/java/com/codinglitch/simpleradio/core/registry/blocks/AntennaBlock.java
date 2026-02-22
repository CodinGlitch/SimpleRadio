package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.Antennal;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

public class AntennaBlock extends Block implements Antennal {
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


    private static int MAX_DISTANCE = 8;
    public static void onLexiconRevision() {
        MAX_DISTANCE = SimpleRadioLibrary.SERVER_CONFIG.antenna.maxDistance;
    }

    @Override
    public int getMaxDistance() {
        return MAX_DISTANCE;
    }

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


}
