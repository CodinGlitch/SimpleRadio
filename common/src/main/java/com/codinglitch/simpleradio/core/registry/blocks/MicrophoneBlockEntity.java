package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.Listening;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MicrophoneBlockEntity extends AuditoryBlockEntity implements Listening {
    public boolean isActive = false;
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
        if (!blockEntity.isActive && blockEntity.id != null) {
            blockEntity.activate();
        }

        if (blockEntity.listener != null) {
            blockEntity.listener.active = blockEntity.listening;
        }
    }

    public boolean isListening() {
        return listening;
    }
    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public void inactivate() {
        if (this.isActive) {
            stopListening();
        }

        this.isActive = false;
    }
    public void activate() {
        WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.listener = SimpleRadioBlocks.MICROPHONE.getOrCreateListener(location, this.id, this.getBlockState());
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
