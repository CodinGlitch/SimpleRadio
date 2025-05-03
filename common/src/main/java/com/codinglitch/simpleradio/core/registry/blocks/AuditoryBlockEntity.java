package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.api.central.Socket;
import com.codinglitch.simpleradio.radio.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A block entity which interacts with audio in some way;
 */
public abstract class AuditoryBlockEntity extends BlockEntity implements Socket {
    public Frequency frequency;

    public UUID id;

    @Nullable
    public RadioReceiver receiver;

    @Nullable
    public RadioTransmitter transmitter;

    @Nullable
    public RadioListener listener;

    @Nullable
    public RadioSpeaker speaker;

    public AuditoryBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (this.id == null && !level.isClientSide) {
            this.id = UUID.randomUUID();
        }
    }

    @Override
    public RadioRouter getRouter() {
        return Stream.of(listener, speaker, transmitter, receiver).filter(Objects::nonNull).findFirst().orElseGet(() -> {
            if (this.id != null && this.hasLevel()) return RadioManager.getInstance().getRouterSided(this.id, this.level.isClientSide);
            return null;
        });
    }

    public Vec3 getConnectionPosition() {
        return this.getBlockPos().getCenter();
    }

    public void loadFromItem(ItemStack stack) {
        loadTag(stack.getOrCreateTag());
    }

    public void loadTag(CompoundTag tag) {
        if (tag.contains("frequency")) {
            String frequencyName = tag.getString("frequency");
            Frequency.Modulation modulation = Frequency.modulationOf(tag.getString("modulation"));
            this.frequency = Frequency.getOrCreateFrequency(frequencyName, modulation);
        }

        if (tag.contains("uuid")) {
            this.id = tag.getUUID("uuid");
        }
    }

    public void saveTag(CompoundTag tag) {
        if (this.frequency != null) {
            tag.putString("frequency", this.frequency.frequency);
            tag.putString("modulation", this.frequency.modulation.shorthand);
        }

        if (this.id != null) {
            tag.putUUID("uuid", this.id);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        saveTag(tag);
        super.saveAdditional(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
