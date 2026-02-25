package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.central.Routing;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.radio.RadioRouter;
import com.codinglitch.simpleradio.routers.Router;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class InsulatorBlock extends BaseEntityBlock implements Routing {
    public static final MapCodec<InsulatorBlock> CODEC = simpleCodec(InsulatorBlock::new);

    @Override
    public MapCodec<InsulatorBlock> codec() {
        return CODEC;
    }

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty EMPTY = BooleanProperty.create("empty");
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");

    private static final VoxelShape TOP_SHAPE = Block.box(6.0, 1.0, 5.0, 10.0, 7.0, 11.0);
    private static final VoxelShape TOP_ROTATED_SHAPE = Block.box(5.0, 1.0, 6.0, 11.0, 7.0, 10.0);

    private static final VoxelShape BOTTOM_SHAPE = Block.box(6.0, 9.0, 5.0, 10.0, 15.0, 11.0);
    private static final VoxelShape BOTTOM_ROTATED_SHAPE = Block.box(5.0, 9.0, 6.0, 11.0, 15.0, 10.0);

    private static final VoxelShape NORTH_SHAPE = Block.box(6.0, 5.0, 9.0, 10.0, 11.0, 15.0);
    private static final VoxelShape NORTH_ROTATED_SHAPE = Block.box(5.0, 6.0, 9.0, 11.0, 10.0, 15.0);

    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0, 5.0, 1.0, 10.0, 11.0, 7);
    private static final VoxelShape SOUTH_ROTATED_SHAPE = Block.box(5.0, 6.0, 1.0, 11.0, 10.0, 7.0);

    private static final VoxelShape WEST_SHAPE = Block.box(9.0, 5.0, 6.0, 15.0, 11.0, 10.0);
    private static final VoxelShape WEST_ROTATED_SHAPE = Block.box(9.0, 6.0, 5.0, 15.0, 10.0, 11.0);

    private static final VoxelShape EAST_SHAPE = Block.box(1.0, 5.0, 6.0, 7.0, 11.0, 10.0);
    private static final VoxelShape EAST_ROTATED_SHAPE = Block.box(1.0, 6.0, 5.0, 7.0, 10.0, 11.0);

    public InsulatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(ROTATED, false).setValue(EMPTY, true));
    }

    @Override
    public Router getOrCreateRouter(WorldlyPosition location, UUID id, BlockState state) {
        Router router = SimpleRadioApi.getRouterSided(id, location.isClientSide());
        if (router != null) return router;

        router = new RadioRouter(id);

        router.setLink(this.getClass());
        router.setPosition(location);

        Vec3i normal = state.getValue(InsulatorBlock.FACING).getOpposite().getNormal();
        router.setConnectionOffset(new Vec3(normal.getX()*0.2f, normal.getY()*0.2f, normal.getZ()*0.2f));

        // Allow distribution through wires
        router.allowDistribution();

        SimpleRadioApi.registerRouterSided(router, location.isClientSide(), null);

        return router;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        super.createBlockStateDefinition(stateBuilder.add(FACING, ROTATED, EMPTY));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState()
                .setValue(FACING, context.getClickedFace());

        if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
            Direction[] directions = context.getNearestLookingDirections();

            Direction face = context.getClickedFace();
            state = switch (face) {
                case NORTH, EAST, SOUTH, WEST -> {
                    for (Direction direction : directions) {
                        if (direction.getAxis().test(face)) continue;
                        yield state.setValue(ROTATED, direction.getAxis().isHorizontal());
                    }
                    yield state;
                }
                case UP, DOWN -> {
                    for (Direction direction : directions) {
                        if (direction.getAxis().isVertical()) continue;
                        yield state.setValue(ROTATED, direction == Direction.WEST || direction == Direction.EAST);
                    }
                    yield state;
                }
            };
            return state;
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor level, BlockPos pos, BlockPos pos1) {
        return state.getValue(FACING).getOpposite() == direction && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, state1, level, pos, pos1);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING).getOpposite();
        return Block.canSupportCenter(level, pos.relative(direction), direction.getOpposite());
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        boolean rotated = state.getValue(ROTATED);
        return switch (state.getValue(FACING)) {
            case UP -> rotated ? TOP_ROTATED_SHAPE : TOP_SHAPE;
            case DOWN -> rotated ? BOTTOM_ROTATED_SHAPE : BOTTOM_SHAPE;
            case NORTH -> rotated ? NORTH_ROTATED_SHAPE : NORTH_SHAPE;
            case SOUTH -> rotated ? SOUTH_ROTATED_SHAPE : SOUTH_SHAPE;
            case WEST -> rotated ? WEST_ROTATED_SHAPE : WEST_SHAPE;
            case EAST -> rotated ? EAST_ROTATED_SHAPE : EAST_SHAPE;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InsulatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, SimpleRadioBlockEntities.INSULATOR, InsulatorBlockEntity::tick);
    }
}
