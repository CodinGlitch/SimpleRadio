package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.central.*;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TransmitterBlockEntity extends CatalyzingBlockEntity implements Transmitting {
    public boolean isActive = false;
    public boolean isDirty = true;
    public int antennaPower = 0;

    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.TRANSMITTER, pos, state);
    }

    @Override
    public BlockPos getAdaptorLocation() {
        return getBlockPos().relative(getBlockState().getValue(ReceiverBlock.FACING).getOpposite());
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
    public void loadTag(CompoundTag tag) {
        //inactivate();
        super.loadTag(tag);
    }
    @Override
    public void saveTag(CompoundTag tag) {
        super.saveTag(tag);
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

    @Override
    public void markDirty() {
        this.isDirty = true;
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, TransmitterBlockEntity blockEntity) {
        if (blockEntity.frequency != null && blockEntity.id != null && !blockEntity.isActive) {
            blockEntity.activate();
        }

        CatalyzingBlockEntity.tick(level, pos, blockState, blockEntity);

        if (!blockEntity.catalyzed) return;

        if (blockEntity.isDirty) {
            blockEntity.antennaPower = blockEntity.calculateAntennaPower(blockEntity.getAdaptorLocation(), level);
            level.sendBlockUpdated(pos, blockState, blockState, 2);

            blockEntity.isDirty = false;
        }
    }

    public void inactivate() {
        if (this.frequency != null) {
            stopTransmitting(frequency.frequency, frequency.modulation, this.id);
        }

        this.isActive = false;
    }

    public void activate() {
        WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        if (!level.isClientSide) {
            this.transmitter = SimpleRadioBlocks.TRANSMITTER.getOrCreateTransmitter(location, frequency, id, this.getBlockState());

            level.playSound(
                    null, location.x, location.y, location.z,
                    SimpleRadioSounds.RADIO_OPEN,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        } else {
            this.transmitter = new RadioTransmitter(frequency, location, id);
            ClientRadioManager.registerRouter(transmitter);
        }

        this.isActive = true;
    }

    public int getAntennaPower() {
        return antennaPower;
    }
}
