package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.radio.RadioListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FrequencerBlockEntity extends BlockEntity {
    public Frequency frequency;
    public List<String> listeners = new ArrayList<>();
    public List<String> receivers = new ArrayList<>();
    public List<String> frequencies = new ArrayList<>();


    public FrequencerBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.FREQUENCER, pos, state);
    }

    private static String parse(@Nullable Entity owner, WorldlyPosition location) {
        if (owner == null) {
            BlockPos blockPos = location.blockPos();
            BlockEntity listenerBlock = location.level.getBlockEntity(blockPos);
            if (listenerBlock != null) {
                return listenerBlock.getBlockState().getBlock().getName().getString()+" at "+blockPos.toShortString();
            }
        } else {
            return owner.getDisplayName().getString();
        }

        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, FrequencerBlockEntity blockEntity) {
        if (Math.round(level.getGameTime()) % 20 == 0 && !level.isClientSide) {
            blockEntity.frequencies.clear();
            blockEntity.listeners.clear();
            blockEntity.receivers.clear();

            //---- Revalidation ----\\
            if (blockEntity.frequency != null) {
                Frequency frequency = Frequency.getFrequency(blockEntity.frequency.frequency, blockEntity.frequency.modulation);
                if (frequency != blockEntity.frequency && frequency != null)
                    blockEntity.frequency = frequency;
            }

            if (blockEntity.frequency != null) {
                //---- Receiver gathering and parsing ----\\
                for (RadioReceiver receiver : blockEntity.frequency.receivers) {
                    String name = parse(null, receiver.location);
                    if (name != null) blockEntity.receivers.add(name);
                }

                level.sendBlockUpdated(pos, blockState, blockState, 2);
            } else {
                if (level.getBlockState(blockEntity.getBlockPos().below()).is(Blocks.DIAMOND_BLOCK)) {
                    //---- Listener gathering ----\\
                    List<RadioListener> listeners = RadioListener.getListeners();
                    for (RadioListener listener : listeners) {
                        String name = parse(listener.owner, listener.location);
                        if (name != null) blockEntity.listeners.add(name);
                    }
                } else {
                    //---- Frequency gathering ----\\
                    List<Frequency> frequencies = Frequency.getFrequencies();
                    for (Frequency frequency : frequencies) {
                        blockEntity.frequencies.add(frequency.frequency + frequency.modulation.shorthand);
                    }
                }

                level.sendBlockUpdated(pos, blockState, blockState, 2);
            }
        }
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveTag(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        CompoundTag tag = new CompoundTag();
        saveTag(tag);
        return ClientboundBlockEntityDataPacket.create(this, blockEntity -> tag);
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

    public void loadTag(CompoundTag tag) {
        if (tag.contains("frequency")) {
            String frequencyName = tag.getString("frequency");
            Frequency.Modulation modulation = Frequency.modulationOf(tag.getString("modulation"));
            this.frequency = Frequency.getOrCreateFrequency(frequencyName, modulation);
        } else {
            this.frequency = null;
        }

        CompoundTag receivers = tag.getCompound("receivers");
        this.receivers.clear();
        for (String key : receivers.getAllKeys()) {
            this.receivers.add(receivers.getString(key));
        }

        CompoundTag listeners = tag.getCompound("listeners");
        this.listeners.clear();
        for (String key : listeners.getAllKeys()) {
            this.listeners.add(listeners.getString(key));
        }

        CompoundTag frequencies = tag.getCompound("frequencies");
        this.frequencies.clear();
        for (String key : frequencies.getAllKeys()) {
            this.frequencies.add(frequencies.getString(key));
        }
    }

    public void saveTag(CompoundTag tag) {
        if (this.frequency != null) {
            tag.putString("frequency", this.frequency.frequency);
            tag.putString("modulation", this.frequency.modulation.shorthand);
        }

        CompoundTag receivers = new CompoundTag();
        for (int i = 0; i < this.receivers.size(); i++) {
            receivers.putString(String.valueOf(i), this.receivers.get(i));
        }
        tag.put("receivers", receivers);

        CompoundTag listeners = new CompoundTag();
        for (int i = 0; i < this.listeners.size(); i++) {
            listeners.putString(String.valueOf(i), this.listeners.get(i));
        }
        tag.put("listeners", listeners);

        CompoundTag frequencies = new CompoundTag();
        for (int i = 0; i < this.frequencies.size(); i++) {
            frequencies.putString(String.valueOf(i), this.frequencies.get(i));
        }
        tag.put("frequencies", frequencies);
    }
}
