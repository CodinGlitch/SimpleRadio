package com.codinglitch.simpleradio.compat.phases;

import com.codinglitch.intermediator.Phase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class RetrieveAccessoryPhase implements Phase {
    public ItemStack stack = ItemStack.EMPTY;
    public Player player;
    public Predicate<ItemStack> filter;

    public RetrieveAccessoryPhase(Player player, Predicate<ItemStack> filter) {
        this.player = player;
        this.filter = filter;
    }
}
