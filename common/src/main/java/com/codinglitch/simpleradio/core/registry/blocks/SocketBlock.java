package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.central.Routing;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SocketBlock extends BaseEntityBlock implements Routing {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape TOP_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 5.0, 10.0);
    private static final VoxelShape BOTTOM_SHAPE = Block.box(6.0, 11.0, 6.0, 10.0, 16.0, 10.0);

    private static final VoxelShape NORTH_SHAPE = Block.box(6.0, 6.0, 11.0, 10.0, 10.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 5.0);
    
    private static final VoxelShape EAST_SHAPE = Block.box(0.0, 6.0, 6.0, 5.0, 10.0, 10.0);
    private static final VoxelShape WEST_SHAPE = Block.box(11.0, 6.0, 6.0, 16.0, 10.0, 10.0);

    public SocketBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RadioRouter getOrCreateRouter(WorldlyPosition location, UUID id, BlockState state) {
        RadioRouter router = RadioManager.getRouterSided(id, location.isClientSide());
        if (router != null) return router;

        router = new RadioRouter(id);

        router.link = this.getClass();
        router.location = location;

        Vec3i normal = state.getValue(SocketBlock.FACING).getOpposite().getNormal();
        router.connectionOffset = new Vec3(normal.getX()*0.2f, normal.getY()*0.2f, normal.getZ()*0.2f);

        // Allow distribution through wires
        router.allowDistribution();

        RadioManager.registerRouterSided(router, location.isClientSide(), null);

        return router;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        super.createBlockStateDefinition(stateBuilder.add(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace());
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> BOTTOM_SHAPE;
            case UP -> TOP_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
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
        return new SocketBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, SimpleRadioBlockEntities.SOCKET, SocketBlockEntity::tick);
    }
}
