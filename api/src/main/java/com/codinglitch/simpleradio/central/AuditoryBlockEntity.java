package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.routers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

import static com.codinglitch.simpleradio.core.SimpleRadioComponents.*;

/**
 * A block entity which interacts with audio in some way;
 * <p>
 * Although this is not required to be extended, it provides helper methods and overrides
 * to simplify the creation of auditory blocks.
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

    public boolean active = false;

    public AuditoryBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    /**
     * Checks if this block should be deactivated due to an invalid router.
     * <p>
     * Ideally, you should call this every tick.
     */
    public void validate() {
        if (receiver != null && !receiver.isValid()) this.deactivate();
        else if (transmitter != null && !transmitter.isValid()) this.deactivate();
        else if (listener != null && !listener.isValid()) this.deactivate();
        else if (speaker != null && !speaker.isValid()) this.deactivate();
    }

    /**
     * This is called whenever the block is initially activated (placed, on world load, chunk loaded, etc.)
     * Use this to create and register the associated routers for this block.
     */
    public void activate() {
        active = true;
    }

    /**
     * This is called whenever the block is deactivated (chunk unloaded, block broken, etc.)
     * Use this to clean up the associated routers.
     * <p>
     * This method will also set all invalid routers to null.
     */
    public void deactivate() {
        active = false;

        if (receiver != null && !receiver.isValid()) receiver = null;
        if (transmitter != null && !transmitter.isValid()) transmitter = null;
        if (listener != null && !listener.isValid()) speaker = null;
        if (speaker != null && !speaker.isValid()) speaker = null;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (this.id == null && !level.isClientSide) {
            this.id = UUID.randomUUID();
        }
    }

    @Override
    public void setRemoved() {
        this.deactivate();
        super.setRemoved();
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

    public static CompoundTag tagFromComponents(ItemStack stack, CompoundTag tag) {
        if (stack.has(FREQUENCY))  tag.putString("frequency",  stack.get(FREQUENCY));
        if (stack.has(MODULATION)) tag.putString("modulation", stack.get(MODULATION).shorthand);
        if (stack.has(REFERENCE))  tag.putUUID("reference",    stack.get(REFERENCE));
        return tag;
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider provider) {
        if (this.frequency != null) {
            stack.set(FREQUENCY, this.frequency.getFrequency());
            stack.set(MODULATION, this.frequency.getModulation());
        }

        if (this.id != null) {
            stack.set(REFERENCE, this.id);
        }
    }

    public void loadFromItem(ItemStack stack) {
        loadTag(tagFromComponents(stack, new CompoundTag()));
    }

    public void loadTag(CompoundTag tag) {
        SimpleRadioApi api = SimpleRadioApi.getInstance();

        if (tag.contains("frequency")) {
            String frequencyName = tag.getString("frequency");
            Frequency.Modulation modulation = api.frequencies().modulationOf(tag.getString("modulation"));
            this.frequency = api.frequencies().getOrCreate(frequencyName, modulation);
        }

        if (tag.contains("reference")) {
            this.id = tag.getUUID("reference");
        } else if (hasLevel() && !level.isClientSide) {
            this.id = UUID.randomUUID();
        }
    }

    public void saveTag(CompoundTag tag) {
        if (this.frequency != null) {
            tag.putString("frequency", this.frequency.getFrequency());
            tag.putString("modulation", this.frequency.getModulation().shorthand);
        }

        if (this.id != null) {
            tag.putUUID("reference", this.id);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        saveTag(tag);
        super.saveAdditional(tag, provider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
