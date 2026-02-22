package com.codinglitch.simpleradio.core.central;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemHolder<I extends Item> {
    private final I item;
    public ResourceLocation tab;
    public boolean enabled = true;

    protected ItemHolder(I item, ResourceLocation tab) {
        this.item = item;
        this.tab = tab;
    }

    public static <I extends Item> ItemHolder<I> of(I item, ResourceLocation tab) {
        return new ItemHolder<>(item, tab);
    }

    public Item get() {
        return this.item;
    }
}
