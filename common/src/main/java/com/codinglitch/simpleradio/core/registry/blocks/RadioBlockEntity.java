package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.central.*;
import com.codinglitch.simpleradio.client.core.central.AnimationInstance;
import com.codinglitch.simpleradio.core.central.Animatable;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadioBlockEntity extends AuditoryBlockEntity implements Receiving, Speaking, Animatable {
    public int antennaPower = 0;

    private final Map<Integer, AnimationInstance> animations = new HashMap<>();
    public float time = 0;
    public static final int PLAYING = 0;

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.RADIO, pos, state);

        allocate(PLAYING);
    }

    @Override
    public Map<Integer, AnimationInstance> getStates() {
        return animations;
    }
    @Override
    public float getTime() {
        return time;
    }
    @Override
    public void setTime(float time) {
        this.time = time;
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && this.speaker != null) {
            level.playSound(
                    null, speaker.getPosition().x, speaker.getPosition().y, speaker.getPosition().z,
                    SimpleRadioSounds.RADIO_CLOSE,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }

        super.setRemoved();
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        loadTag(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        saveTag(tag);
        super.saveAdditional(tag, provider);
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, RadioBlockEntity blockEntity) {
        if (blockEntity.frequency != null && blockEntity.id != null && !blockEntity.active) {
            blockEntity.activate();
        }

        if (level.isClientSide) {
            //blockEntity.playingAnimationState.ifStarted(state -> state.start((int) blockEntity.time));

            blockEntity.time += 0.05f;
        } else {

        }
    }

    @Override
    public void deactivate() {
        if (this.active) {
            stopReceiving(frequency.getFrequency(), frequency.getModulation(), id, level.isClientSide);
            stopSpeaking(id, level.isClientSide);
        }

        // Clean up the invalidated routers.
        super.deactivate();
    }

    @Override
    public void activate() {
        CommonSimpleRadio.info("Activating radio with reference {}", id);
        WorldlyPosition location = CompatCore.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.speaker = SimpleRadioBlocks.RADIO.getOrCreateSpeaker(location, id, this.getBlockState());
        this.receiver = SimpleRadioBlocks.RADIO.getOrCreateReceiver(location, this.frequency, id, this.getBlockState());
        if (!level.isClientSide) {
            //TODO: update players of radio state

            level.playSound(
                    null, location.x, location.y, location.z,
                    SimpleRadioSounds.RADIO_OPEN,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }

        receiver.addRouter(speaker);

        // Mark this block as active.
        super.activate();
    }

    @Override
    public int getAntennaPower() {
        return antennaPower;
    }

    @Override
    public List<Wiring> getWires() {
        return getRouter().getWires();
    }
}
