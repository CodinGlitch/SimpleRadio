package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioListener;
import com.codinglitch.simpleradio.radio.RadioTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class AuditoryBlockEntity extends BlockEntity {
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
        this.id = UUID.randomUUID();
    }

    public boolean canConnect() {
        return true;
    }
    public boolean canConnectTo(AuditoryBlockEntity other) {
        return true;
    }

    public static void connectRouters(AuditoryBlockEntity from, AuditoryBlockEntity to) {
        if (from.listener != null) {
            if (to.transmitter != null) {
                from.listener.tryAddRouter(to.transmitter);
                CommonSimpleRadio.info("Connected: Listener -> Transmitter");
            }
            if (to.speaker != null) {
                from.listener.tryAddRouter(to.speaker);
                CommonSimpleRadio.info("Connected: Listener -> Speaker");
            }
        }

        if (from.receiver != null) {
            if (to.speaker != null) {
                from.receiver.tryAddRouter(to.speaker);
                CommonSimpleRadio.info("Connected: Receiver -> Speaker");
            }
        }
    }
    public void connectTo(AuditoryBlockEntity other) {
        if (!this.canConnectTo(other)) return;
        if (!other.canConnectTo(this)) return;

        connectRouters(this, other);
        connectRouters(other, this);

        CommonSimpleRadio.info("connection made");
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

        tag.putUUID("uuid", this.id);
    }
}
