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

public class SpeakerBlockEntity extends CentralBlockEntity implements Receiving, Speaking {
    public boolean isActive = false;

    public SpeakerBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.SPEAKER, pos, state);

        this.id = UUID.randomUUID();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && this.speaker != null) {
            level.playSound(
                    null, speaker.location.x, speaker.location.y, speaker.location.z,
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

    public static void tick(Level level, BlockPos pos, BlockState blockState, SpeakerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (blockEntity.frequency != null && !blockEntity.isActive) {
                blockEntity.activate();
            }
        }
    }

    public void inactivate() {
        if (this.frequency != null) {
            stopSpeaking();
            stopReceiving(frequency.frequency, frequency.modulation, id);
        }

        this.isActive = false;
    }

    public void activate() {
        WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        speaker = startSpeaking(location, id);
        receiver = startReceiving(location, this.frequency, id);

        receiver.routers.add(speaker);

        level.playSound(
                null, speaker.location.x, speaker.location.y, speaker.location.z,
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
