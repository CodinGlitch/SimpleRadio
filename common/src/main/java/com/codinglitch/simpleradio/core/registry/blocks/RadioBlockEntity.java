package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.StaticPosition;
import com.codinglitch.simpleradio.core.central.Transceiving;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.radio.RadioChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class RadioBlockEntity extends BlockEntity implements Transceiving {
    public boolean isListening = false;
    public Frequency frequency;
    public UUID listenerID;

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.RADIO, pos, state);

        this.listenerID = UUID.randomUUID();
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            level.playSound(
                    null, this.worldPosition,
                    SimpleRadioSounds.RADIO_CLOSE,
                    SoundSource.PLAYERS,
                    1f,1f
            );
        }


        if (this.frequency != null)
            stopListening(frequency.frequency, frequency.modulation, listenerID);
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

    public static void tick(Level level, BlockPos pos, BlockState blockState, RadioBlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (blockEntity.frequency != null && !blockEntity.isListening) {
                blockEntity.listen();
            }
        }
    }

    public void listen() {
        RadioChannel channel = listen(frequency.frequency, frequency.modulation, listenerID);
        channel.location = StaticPosition.of(this.worldPosition, (ServerLevel) this.level);

        level.playSound(
                null, this.worldPosition,
                SimpleRadioSounds.RADIO_OPEN,
                SoundSource.PLAYERS,
                1f,1f
        );

        this.isListening = true;
    }

    public void loadFromItem(ItemStack stack) {
        loadTag(stack.getOrCreateTag());
    }

    public void loadTag(CompoundTag tag) {
        if (this.frequency != null) {
            stopListening(frequency.frequency, frequency.modulation, listenerID);
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
