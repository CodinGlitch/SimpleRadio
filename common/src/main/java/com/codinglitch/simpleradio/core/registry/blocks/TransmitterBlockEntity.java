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

import java.util.UUID;

public class TransmitterBlockEntity extends AuditoryBlockEntity implements Transmitting {
    public boolean isActive = false;

    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.TRANSMITTER, pos, state);

        this.id = UUID.randomUUID();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && this.transmitter != null) {
            level.playSound(
                    null, transmitter.location.x, transmitter.location.y, transmitter.location.z,
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

    public static void tick(Level level, BlockPos pos, BlockState blockState, TransmitterBlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (blockEntity.frequency != null && !blockEntity.isActive) {
                blockEntity.activate();
            }
        }
    }

    public void inactivate() {
        if (this.frequency != null) {
            stopTransmitting(frequency.frequency, frequency.modulation, id);
        }

        this.isActive = false;
    }

    public void activate() {
        WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.transmitter = startTransmitting(location, this.frequency, id);

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
