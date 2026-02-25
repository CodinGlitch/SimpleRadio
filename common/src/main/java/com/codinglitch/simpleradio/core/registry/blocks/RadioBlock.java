package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.*;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioFrequencing;
import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.routers.Receiver;
import com.codinglitch.simpleradio.routers.Speaker;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RadioBlock extends BaseEntityBlock implements Routing, Speaking, Receiving {
    public static final MapCodec<RadioBlock> CODEC = simpleCodec(RadioBlock::new);

    @Override
    public MapCodec<RadioBlock> codec() {
        return CODEC;
    }

    public static final int MAX_ROTATION_INDEX = RotationSegment.getMaxSegmentIndex();
    private static final int MAX_ROTATIONS = MAX_ROTATION_INDEX + 1;
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 7.0, 14.0);

    public RadioBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ROTATION, 0));
    }

    @Override
    public Speaker getOrCreateSpeaker(WorldlyPosition location, UUID id, BlockState state) {
        Speaker speaker = startSpeaking(location, id);
        speaker.setRange(SimpleRadioLibrary.SERVER_CONFIG.radio.speakingRange);
        speaker.setCategory(CommonRadioPlugin.RADIOS_CATEGORY);

        return speaker;
    }

    @Override
    public Receiver getOrCreateReceiver(WorldlyPosition location, Frequency frequency, UUID id, BlockState state) {
        Receiver receiver = startReceiving(location, frequency, id);

        // Allow distribution through wires
        receiver.allowDistribution();
        receiver.frequencingType(SimpleRadioFrequencing.RADIO);

        return receiver;
    }

    public float getYRotationDegrees(BlockState state) {
        return RotationSegment.convertToDegrees(state.getValue(ROTATION));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        super.createBlockStateDefinition(stateBuilder.add(ROTATION));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation() + 180.0F));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), MAX_ROTATIONS));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), MAX_ROTATIONS));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState $$0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return 0;
    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof RadioBlockEntity radioBlockEntity)
            radioBlockEntity.saveToItem(stack, builder.getLevel().registryAccess());

        return List.of(stack);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity entity, ItemStack stack) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof RadioBlockEntity radioBlockEntity) {
            radioBlockEntity.loadFromItem(stack);
        }

        super.setPlacedBy(level, blockPos, blockState, entity, stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, SimpleRadioBlockEntities.RADIO, RadioBlockEntity::tick);
    }
}
