package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.routers.*;
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
    public Receiver receiver;

    @Nullable
    public Transmitter transmitter;

    @Nullable
    public Listener listener;

    @Nullable
    public Speaker speaker;

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
    public Router getRouter() {
        return Stream.of(listener, speaker, transmitter, receiver).filter(Objects::nonNull).findFirst().orElseGet(() -> {
            if (this.id != null && this.hasLevel()) return SimpleRadioApi.getRouterSided(this.id, this.level.isClientSide);
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
        SimpleRadioApi api = this.hasLevel() ? SimpleRadioApi.getInstance(this.level.isClientSide) : ServerSimpleRadioApi.getInstance();

        if (tag.contains("frequency")) {
            String frequencyName = tag.getString("frequency");
            Frequency.Modulation modulation = api.frequencies().modulationOf(tag.getString("modulation"));
            this.frequency = api.frequencies().getOrCreate(frequencyName, modulation);
        }

        if (tag.contains("uuid")) {
            this.id = tag.getUUID("uuid");
        }
    }

    public void saveTag(CompoundTag tag) {
        if (this.frequency != null) {
            tag.putString("frequency", this.frequency.getFrequency());
            tag.putString("modulation", this.frequency.getModulation().shorthand);
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
