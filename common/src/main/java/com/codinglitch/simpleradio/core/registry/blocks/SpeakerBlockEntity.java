package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.AuditoryBlockEntity;
import com.codinglitch.simpleradio.central.Speaking;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
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

public class SpeakerBlockEntity extends AuditoryBlockEntity implements Speaking {

    public SpeakerBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.SPEAKER, pos, state);
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
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        loadTag(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        saveTag(tag);
        super.saveAdditional(tag, provider);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpeakerBlockEntity blockEntity) {
        if (!blockEntity.active && blockEntity.id != null) {
            blockEntity.activate();
        }

        if (blockEntity.level == null) return;
        if (blockEntity.speaker != null && blockEntity.speaker.getActivityTime() >= 0) {
            if (blockEntity.speaker.getActivityTime() % SimpleRadioLibrary.SERVER_CONFIG.speaker.redstonePolling == 0) {
                level.updateNeighborsAt(pos, SimpleRadioBlocks.SPEAKER);
            }
            if (SimpleRadioLibrary.CLIENT_CONFIG.speaker.particleInterval != 0) {
                if (blockEntity.level.isClientSide && blockEntity.speaker.getActivityTime() % SimpleRadioLibrary.CLIENT_CONFIG.speaker.particleInterval == 0) {
                    ClientRadioManager.handleSpeakParticle(state, blockEntity);
                }
            }
        }
    }

    @Override
    public void deactivate() {
        if (active) {
            stopSpeaking(id, level.isClientSide);
        }

        // Clean up the invalidated routers.
        super.deactivate();
    }

    @Override
    public void activate() {
        CommonSimpleRadio.info("Activating speaker with reference {}", id);
        WorldlyPosition location = CompatCore.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.speaker = SimpleRadioBlocks.SPEAKER.getOrCreateSpeaker(location, id, this.getBlockState());
        this.speaker.setPosition(location);

        if (!level.isClientSide) {
            level.playSound(
                    null, location.x, location.y, location.z,
                    SimpleRadioSounds.RADIO_OPEN,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }

        // Mark this block as active.
        super.activate();
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
    }
}
