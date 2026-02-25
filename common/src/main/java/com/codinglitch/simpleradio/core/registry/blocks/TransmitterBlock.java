package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.central.*;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioFrequencing;
import com.codinglitch.simpleradio.routers.Transmitter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TransmitterBlock extends BaseEntityBlock implements Routing, Transmitting {
    public static final MapCodec<TransmitterBlock> CODEC = simpleCodec(TransmitterBlock::new);

    @Override
    public MapCodec<TransmitterBlock> codec() {
        return CODEC;
    }

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(2.0, 0.0, 4.0, 14.0, 9.0, 16.0), Block.box(1.0, 9.0, 3.0, 15.0, 12.0, 16.0));
    private static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(0.0, 0.0, 2.0, 12.0, 9.0, 14.0), Block.box(0.0, 9.0, 1.0, 13.0, 12.0, 15.0));
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(2.0, 0.0, 0.0, 14.0, 9.0, 12.0), Block.box(1.0, 9.0, 0.0, 15.0, 12.0, 13.0));
    private static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(4.0, 0.0, 2.0, 16.0, 9.0, 14.0), Block.box(3.0, 9.0, 1.0, 16.0, 12.0, 15.0));

    public TransmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public Transmitter getOrCreateTransmitter(WorldlyPosition location, Frequency frequency, UUID id, BlockState state) {
        Transmitter transmitter = startTransmitting(location, frequency, id);

        transmitter.frequencingType(SimpleRadioFrequencing.TRANSMITTER);

        return transmitter;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        super.createBlockStateDefinition(stateBuilder.add(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            default -> SHAPE_WEST;
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

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof AuditoryBlockEntity auditoryBlockEntity)
            auditoryBlockEntity.saveToItem(stack, builder.getLevel().registryAccess());

        return List.of(stack);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity entity, ItemStack stack) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof AuditoryBlockEntity auditoryBlockEntity) {
            auditoryBlockEntity.loadFromItem(stack);
        }

        super.setPlacedBy(level, blockPos, blockState, entity, stack);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return super.useItemOn(stack, state, level, pos, player, hand, result);

        if (blockEntity instanceof CatalyzingBlockEntity catalyzingBlock)  {
            ItemInteractionResult interactionResult = catalyzingBlock.trySwapCatalyst(state, level, pos, player, hand, result);
            if (interactionResult != null) return interactionResult;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, result);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransmitterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, SimpleRadioBlockEntities.TRANSMITTER, TransmitterBlockEntity::tick);
    }
}
