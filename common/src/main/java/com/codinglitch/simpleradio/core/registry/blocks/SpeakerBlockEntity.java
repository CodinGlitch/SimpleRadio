package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.api.central.Speaking;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioParticles;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class SpeakerBlockEntity extends AuditoryBlockEntity implements Speaking {
    public boolean isActive = false;

    public SpeakerBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.SPEAKER, pos, state);
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

    public static void tick(Level level, BlockPos pos, BlockState state, SpeakerBlockEntity blockEntity) {
        if (!blockEntity.isActive && blockEntity.id != null) {
            blockEntity.activate();
        }

        if (blockEntity.level == null) return;
        if (blockEntity.level.isClientSide && blockEntity.level.getGameTime() % 10 == 0) {
            ClientRadioManager.handleSpeakParticle(state, blockEntity);
        }
    }

    public void inactivate() {
        if (this.isActive) {
            stopSpeaking();
            //stopReceiving(frequency.frequency, frequency.modulation, id);
        }

        this.isActive = false;
    }

    public void activate() {
        WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.speaker = SimpleRadioBlocks.SPEAKER.getOrCreateSpeaker(location, id, this.getBlockState());
        if (!level.isClientSide) {
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
    public void loadTag(CompoundTag tag) {
        inactivate();
        super.loadTag(tag);
    }
}
