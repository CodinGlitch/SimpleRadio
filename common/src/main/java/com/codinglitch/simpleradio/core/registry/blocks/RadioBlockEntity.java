package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.central.*;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RadioBlockEntity extends AuditoryBlockEntity implements Receiving, Speaking {
    public boolean isActive = false;
    public int antennaPower = 0;
    public float time = 0;


    public int playingTime = 0;

    public final AnimationState playingAnimationState = new AnimationState();

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.RADIO, pos, state);
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
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
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
        if (blockEntity.frequency != null && blockEntity.id != null && !blockEntity.isActive) {
            blockEntity.activate();
        }

        if (level.isClientSide) {
            blockEntity.playingAnimationState.ifStarted(state -> state.start((int) blockEntity.time));

            blockEntity.time += 0.05f;
        } else {
            if (blockEntity.playingTime > 0) {
                blockEntity.playingTime--;
            } else if (blockEntity.playingTime == 0) {
                blockEntity.playingTime = -1;

                //TODO: update players of radio state
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

        this.speaker = SimpleRadioBlocks.RADIO.getOrCreateSpeaker(location, id, this.getBlockState());
        this.receiver = SimpleRadioBlocks.RADIO.getOrCreateReceiver(location, this.frequency, id, this.getBlockState());

        receiver.routers.add(speaker);

        if (!level.isClientSide) {
            //TODO: update players of radio state

            level.playSound(
                    null, location.x, location.y, location.z,
                    SimpleRadioSounds.RADIO_OPEN,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }

        this.isActive = true;
    }

    @Override
    public int getAntennaPower() {
        return antennaPower;
    }
}
