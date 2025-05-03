package com.codinglitch.simpleradio.datagen;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;

import java.util.Set;

public class CommonLootTableProvider extends BlockLootSubProvider {

    protected CommonLootTableProvider(Set<Item> items, FeatureFlagSet featureFlagSet) {
        super(items, featureFlagSet);
    }

    @Override
    public void generate() {

    }
}
