package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.routers.Listener;
import com.codinglitch.simpleradio.routers.Receiver;
import com.codinglitch.simpleradio.routers.Transmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
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
    public List<String> frequencings = new ArrayList<>();
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
            blockEntity.frequencings.clear();

            //---- Revalidation ----\\
            if (blockEntity.frequency != null) {
                Frequency frequency = ServerSimpleRadioApi.getInstance().frequencies().get(blockEntity.frequency.getFrequency(), blockEntity.frequency.getModulation());
                if (frequency != blockEntity.frequency && frequency != null)
                    blockEntity.frequency = frequency;
            }

            if (blockEntity.frequency != null) {
                //---- Receiver gathering and parsing ----\\
                for (Receiver receiver : blockEntity.frequency.getReceivers()) {
                    String name = parse(receiver.getOwner(), receiver.getPosition());
                    if (name != null) blockEntity.frequencings.add(name);
                }

                //---- Transmitter gathering and parsing ----\\
                for (Transmitter transmitter : blockEntity.frequency.getTransmitters()) {
                    String name = parse(transmitter.getOwner(), transmitter.getPosition());
                    if (name != null) blockEntity.frequencings.add(name);
                }

                level.sendBlockUpdated(pos, blockState, blockState, 2);
            } else {
                BlockState state = level.getBlockState(blockEntity.getBlockPos().below());
                if (state.is(Blocks.DIAMOND_BLOCK)) {
                    //---- Listener gathering ----\\
                    List<Listener> listeners = ServerSimpleRadioApi.getInstance().listeners().get();
                    for (Listener listener : listeners) {
                        String name = parse(listener.getOwner(), listener.getPosition());
                        if (name != null) blockEntity.listeners.add(name);
                    }
                } else {
                    //---- Frequency gathering ----\\
                    List<Frequency> frequencies = ServerSimpleRadioApi.getInstance().frequencies().get();
                    for (Frequency frequency : frequencies) {
                        blockEntity.frequencies.add(frequency.getFrequency() + frequency.getModulation().shorthand);
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
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveTag(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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

    public void loadTag(CompoundTag tag) {
        if (tag.contains("frequency")) {
            String frequencyName = tag.getString("frequency");
            Frequency.Modulation modulation = ServerSimpleRadioApi.getInstance().frequencies().modulationOf(tag.getString("modulation"));
            this.frequency = ServerSimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation);
        } else {
            this.frequency = null;
        }

        CompoundTag receivers = tag.getCompound("receivers");
        this.frequencings.clear();
        for (String key : receivers.getAllKeys()) {
            this.frequencings.add(receivers.getString(key));
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
            tag.putString("frequency", this.frequency.getFrequency());
            tag.putString("modulation", this.frequency.getModulation().shorthand);
        }

        CompoundTag receivers = new CompoundTag();
        for (int i = 0; i < this.frequencings.size(); i++) {
            receivers.putString(String.valueOf(i), this.frequencings.get(i));
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
