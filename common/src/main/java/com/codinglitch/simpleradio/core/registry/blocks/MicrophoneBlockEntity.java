package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.central.*;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MicrophoneBlockEntity extends AuditoryBlockEntity implements Listening {
    public boolean isActive = false;

    public MicrophoneBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.MICROPHONE, pos, state);
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && this.listener != null) {
            level.playSound(
                    null, listener.location.x, listener.location.y, listener.location.z,
                    SimpleRadioSounds.RADIO_CLOSE,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }

        inactivate();

        super.setRemoved();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadTag(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        saveTag(tag);
        super.saveAdditional(tag);
    }

    @Override
    public void saveToItem(ItemStack stack) {
        saveTag(stack.getOrCreateTag());
        super.saveToItem(stack);
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, MicrophoneBlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (blockEntity.frequency != null && !blockEntity.isActive) {
                blockEntity.activate();
            }
        }
    }

    public void inactivate() {
        if (this.frequency != null) {
            stopListening();
        }

        this.isActive = false;
    }
    public void activate() {
        WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.listener = startListening(location, id);
        //transmitter = startTransmitting(location, this.frequency, id);

        //listener.routers.add(transmitter);

        level.playSound(
                null, location.x, location.y, location.z,
                SimpleRadioSounds.RADIO_OPEN,
                SoundSource.PLAYERS,
                1f, 1f
        );

        this.isActive = true;
    }

    @Override
    public void loadTag(CompoundTag tag) {
        inactivate();
        super.loadTag(tag);
    }
}
