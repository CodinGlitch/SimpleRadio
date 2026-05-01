package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.AuditoryBlockEntity;
import com.codinglitch.simpleradio.central.Listening;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MicrophoneBlockEntity extends AuditoryBlockEntity implements Listening {
    private boolean listening = true;
    public float tilt = 1.5f;
    public float currentTilt = tilt - 1.5f;

    public MicrophoneBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.MICROPHONE, pos, state);
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && this.listener != null) {
            level.playSound(
                    null, listener.getPosition().x, listener.getPosition().y, listener.getPosition().z,
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

    public static void tick(Level level, BlockPos pos, BlockState state, MicrophoneBlockEntity blockEntity) {
        if (!blockEntity.active && blockEntity.id != null) {
            blockEntity.activate();
        }

        if (blockEntity.level == null) return;
        if (blockEntity.listener != null && blockEntity.listener.getActivityTime() >= 0) {
            if (blockEntity.listener.getActivityTime() % SimpleRadioLibrary.SERVER_CONFIG.microphone.redstonePolling == 0) {
                level.updateNeighborsAt(pos, SimpleRadioBlocks.MICROPHONE);
            }
            if (SimpleRadioLibrary.CLIENT_CONFIG.speaker.particleInterval != 0) {
                if (blockEntity.level.isClientSide && blockEntity.listener.getActivityTime() % SimpleRadioLibrary.CLIENT_CONFIG.microphone.particleInterval == 0) {
                    ClientRadioManager.handleListenParticle(state, blockEntity);
                }
            }
        }
    }

    public boolean isListening() {
        return listening;
    }
    public void setListening(boolean listening) {
        this.listening = listening;
        if (this.listener != null) this.listener.setActive(this.listening);
    }

    @Override
    public void deactivate() {
        if (this.active) {
            stopListening(this.id, level.isClientSide);
        }

        // Clean up the invalidated routers.
        super.deactivate();
    }

    @Override
    public void activate() {
        CommonSimpleRadio.info("Activating microphone with reference {}", id);
        WorldlyPosition location = CompatCore.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.listener = SimpleRadioBlocks.MICROPHONE.getOrCreateListener(location, this.id, this.getBlockState());
        this.listener.setActive(this.listening);

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

        if (tag.contains("tilt")) {
            this.tilt = tag.getFloat("tilt");
        }

        if (tag.contains("listening")) {
            this.setListening(tag.getBoolean("listening"));
        }
    }

    @Override
    public void saveTag(CompoundTag tag) {
        super.saveTag(tag);

        tag.putFloat("tilt", this.tilt);
        tag.putBoolean("listening", this.listening);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
