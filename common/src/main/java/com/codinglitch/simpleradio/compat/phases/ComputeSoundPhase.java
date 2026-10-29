package com.codinglitch.simpleradio.compat.phases;

import com.codinglitch.intermediator.Phase;
import net.minecraft.world.item.ItemStack;

public class ComputeSoundPhase implements Phase {
    public String sound;
    public ItemStack stack;

    public ComputeSoundPhase(ItemStack stack) {
        this.stack = stack;
    }
}
