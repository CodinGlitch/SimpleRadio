package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.api.central.Listening;
import com.codinglitch.simpleradio.api.central.Routing;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.radio.RadioListener;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class MicrophoneBlock extends BaseEntityBlock implements Routing, Listening {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0);

    public MicrophoneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ROTATION, 0));
    }

    public float getYRotationDegrees(BlockState state) {
        return -((float)(state.getValue(StandingSignBlock.ROTATION) * 360) / 16.0f);
    }

    @Override
    public RadioListener getOrCreateListener(WorldlyPosition location, UUID id, BlockState state) {
        RadioListener listener = startListening(location, id);

        listener.range = SimpleRadioLibrary.SERVER_CONFIG.microphone.listeningRange;

        float rotation = (float) Math.toRadians(getYRotationDegrees(state) - 90);
        Vector3f normal = new Vector3f(Mth.cos(rotation), 0, Mth.sin(rotation));
        listener.connectionOffset = new Vec3(normal.x()*0.1f, -0.2f, normal.z()*0.1f);

        // Allow distribution through wires
        listener.allowDistribution();

        return listener;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        super.createBlockStateDefinition(stateBuilder.add(ROTATION));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(ROTATION, Mth.floor((double) ((180.0F + context.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState $$0) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MicrophoneBlockEntity microphone) {
            if (microphone.listener != null) {
                return microphone.listener.getRedstoneMappedActivity();
            }
        }

        return 0;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof RadioBlockEntity radioBlockEntity)
            radioBlockEntity.saveToItem(stack);

        return List.of(stack);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MicrophoneBlockEntity mic) {
            if (player.isCrouching()) {
                mic.tilt = (mic.tilt + 0.1f) % 3;

                if (!level.isClientSide)
                 level.playSound(null, mic.getBlockPos(), SimpleRadioSounds.TILT_MICROPHONE, SoundSource.BLOCKS, 0.1f, 0.9f + level.random.nextFloat()*0.2f);


                return InteractionResult.SUCCESS;
            } else {
                mic.setListening(!mic.isListening());

                if (!level.isClientSide) {
                    float pitch = mic.isListening() ? 1.1f : 0.9f;
                    level.playSound(null, mic.getBlockPos(), SimpleRadioSounds.PRESS_MICROPHONE, SoundSource.BLOCKS, 0.4f, pitch + level.random.nextFloat()*0.1f);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.use(state, level, pos, player, hand, result);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity entity, ItemStack stack) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof MicrophoneBlockEntity microphoneBlockEntity) {
            microphoneBlockEntity.loadFromItem(stack);
        }

        super.setPlacedBy(level, blockPos, blockState, entity, stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MicrophoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, SimpleRadioBlockEntities.MICROPHONE, MicrophoneBlockEntity::tick);
    }
}
