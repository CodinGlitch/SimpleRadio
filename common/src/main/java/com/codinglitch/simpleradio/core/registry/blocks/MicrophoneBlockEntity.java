package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.core.central.*;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioListener;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.UUID;

public class MicrophoneBlockEntity extends FrequencyBlockEntity implements Transmitting {
    public boolean isListening = false;
    public UUID listenerID;

    private RadioListener listener;

    public MicrophoneBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.MICROPHONE, pos, state);

        this.listenerID = UUID.randomUUID();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            Vector3f locationVec = Services.COMPAT.modifyPosition(level, this.worldPosition);
            level.playSound(
                    null, locationVec.x, locationVec.y, locationVec.z,
                    SimpleRadioSounds.RADIO_CLOSE,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }


        if (this.frequency != null)
            stopListening(WorldlyPosition.of(getBlockPos(), level));
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
            if (blockEntity.listener != null) { blockEntity.listener.location = Services.COMPAT.modifyPosition(pos, level); }
            if (blockEntity.frequency != null && !blockEntity.isListening) {
                blockEntity.listen();
            }
        }
    }

    public void listen() {
        listener = startListening(Services.COMPAT.modifyPosition(getBlockPos(), level));

        listener.range = 12;
        listener.acceptor(source -> {
            source.type = RadioSource.Type.TRANSMITTER;
            source.delegate(listenerID);

            Frequency frequency = getFrequency(this);
            if (frequency != null) RadioManager.transmit(source, frequency);
        });

        this.frequency.tryAddTransmitter(listener);

        Vector3f locationVec = Services.COMPAT.modifyPosition(level, this.worldPosition);
        level.playSound(
                null, locationVec.x, locationVec.y, locationVec.z,
                SimpleRadioSounds.RADIO_OPEN,
                SoundSource.PLAYERS,
                1f, 1f
        );

        this.isListening = true;
    }

    public void loadFromItem(ItemStack stack) {
        loadTag(stack.getOrCreateTag());
    }

    public void loadTag(CompoundTag tag) {
        if (this.frequency != null) {
            stopListening(WorldlyPosition.of(getBlockPos(), level));
            this.isListening = false;
        }

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
