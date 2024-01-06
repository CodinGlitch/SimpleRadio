package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.Transceiving;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class RadioBlockEntity extends BlockEntity implements Transceiving {
    public Frequency frequency;
    public UUID listenerID;

    public RadioBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.RADIO, pos, state);

        this.listenerID = UUID.randomUUID();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (this.frequency != null) {
            stopListening(frequency.frequency, frequency.modulation, listenerID);
        }

        this.frequency = Frequency.getOrCreateFrequency(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")));
        listen(frequency.frequency, frequency.modulation, listenerID);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putString("frequency", this.frequency.frequency);
        tag.putString("modulation", this.frequency.modulation.shorthand);
        super.saveAdditional(tag);
    }

    @Override
    public void saveToItem(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("frequency", this.frequency.frequency);
        tag.putString("modulation", this.frequency.modulation.shorthand);
        super.saveToItem(stack);
    }
}
