package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RadiosmitherBlock extends BaseEntityBlock {
    public static final MapCodec<RadiosmitherBlock> CODEC = simpleCodec(RadiosmitherBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<RadiosmitherPart> RADIOSMITHER_PART = EnumProperty.create("part", RadiosmitherPart.class);

    private static final VoxelShape TOP_SHAPE = Block.box(0, 14, 0, 16, 16, 16);
    public static final VoxelShape MAIN_SHAPE = Shapes.or(TOP_SHAPE,
            Block.box(1, 0, 1, 15, 14, 15)
    );

    private static final VoxelShape NORTH_SIDE_SHAPE = Shapes.or(TOP_SHAPE,
            Block.box(1, 0, 1, 3, 14, 3), Block.box(1, 0, 13, 3, 14, 15)
    );
    private static final VoxelShape SOUTH_SIDE_SHAPE = Shapes.or(TOP_SHAPE,
            Block.box(13, 0, 13, 15, 14, 15), Block.box(13, 0, 1, 15, 14, 3)
    );
    private static final VoxelShape EAST_SIDE_SHAPE = Shapes.or(TOP_SHAPE,
            Block.box(1, 0, 1, 3, 14, 3), Block.box(13, 0, 1, 15, 14, 3)
    );
    private static final VoxelShape WEST_SIDE_SHAPE = Shapes.or(TOP_SHAPE,
            Block.box(1, 0, 13, 3, 14, 15), Block.box(13, 0, 13, 15, 14, 15)
    );

    public RadiosmitherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(RADIOSMITHER_PART, RadiosmitherPart.MAIN));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, RADIOSMITHER_PART);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean b) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof Container container) {
                Containers.dropContents(level, pos, container);
                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, level, pos, newState, b);
        }
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide) {
            RadiosmitherPart part = state.getValue(RADIOSMITHER_PART);
            BlockPos mainPos = part == RadiosmitherPart.MAIN ? pos : pos.relative(state.getValue(FACING).getClockWise());

            MenuProvider provider = state.getMenuProvider(level, mainPos);

            if (provider != null) {
                player.openMenu(provider);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (state.getValue(RADIOSMITHER_PART) == RadiosmitherPart.MAIN) {
            return MAIN_SHAPE;
        } else {
            return switch (state.getValue(FACING)) {
                case SOUTH -> SOUTH_SIDE_SHAPE;
                case WEST -> WEST_SIDE_SHAPE;
                case EAST -> EAST_SIDE_SHAPE;
                default -> NORTH_SIDE_SHAPE;
            };
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(RADIOSMITHER_PART) == RadiosmitherPart.MAIN ? new RadiosmitherBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, SimpleRadioBlockEntities.RADIOSMITHER, RadiosmitherBlockEntity::tick);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide) {
            super.playerWillDestroy(level, pos, state, player);
            return state;
        }

        RadiosmitherPart part = state.getValue(RADIOSMITHER_PART);
        BlockPos otherPos = pos.relative(part == RadiosmitherPart.MAIN ? state.getValue(FACING).getCounterClockWise() : state.getValue(FACING).getClockWise());
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.getBlock() instanceof RadiosmitherBlock) {
            level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, otherPos, Block.getId(otherState));
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockPos blockpos = pos.relative(state.getValue(FACING).getCounterClockWise());
            level.setBlock(blockpos, state.setValue(RADIOSMITHER_PART, RadiosmitherPart.SIDE), Block.UPDATE_ALL);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(RADIOSMITHER_PART) == RadiosmitherPart.MAIN ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.relative(state.getValue(FACING).getCounterClockWise())).canBeReplaced();
    }

    public enum RadiosmitherPart implements StringRepresentable {
        MAIN("head"),
        SIDE("side");

        private final String name;

        RadiosmitherPart(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
        public String getSerializedName() {
            return this.name;
        }
    }
}
