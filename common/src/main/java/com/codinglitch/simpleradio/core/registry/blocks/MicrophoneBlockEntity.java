package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.central.*;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioListener;
import com.codinglitch.simpleradio.radio.RadioTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MicrophoneBlockEntity extends CentralBlockEntity implements Transmitting, Listening {
    public boolean isActive = false;

    public RadioTransmitter transmitter;
    public RadioListener listener;

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
            stopTransmitting();
        }

        this.isActive = false;
    }
    public void activate() {
        WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        listener = startListening(location, id);
        transmitter = startTransmitting(location, this.frequency, id);

        listener.routers.add(transmitter);

        level.playSound(
                null, location.x, location.y, location.z,
                SimpleRadioSounds.RADIO_OPEN,
                SoundSource.PLAYERS,
                1f, 1f
        );

        this.isActive = true;
    }

    public void loadFromItem(ItemStack stack) {
        loadTag(stack.getOrCreateTag());
    }

    public void loadTag(CompoundTag tag) {
        inactivate();

        String frequencyName = tag.getString("frequency");
        Frequency.Modulation modulation = Frequency.modulationOf(tag.getString("modulation"));
        this.frequency = Frequency.getOrCreateFrequency(frequencyName, modulation);
    }

    public void saveTag(CompoundTag tag) {
        if (this.frequency == null) return;

        tag.putString("frequency", this.frequency.frequency);
        tag.putString("modulation", this.frequency.modulation.shorthand);
    }
}
