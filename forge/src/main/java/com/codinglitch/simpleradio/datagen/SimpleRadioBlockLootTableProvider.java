package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.Set;

public class SimpleRadioBlockLootTableProvider extends BlockLootSubProvider {

    public SimpleRadioBlockLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, provider);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return SimpleRadioBlocks.BLOCKS.values();
    }

    @Override
    public void generate() {
        //TODO: create way to differentiate self-dropping blocks
        SimpleRadioBlocks.BLOCKS.forEach(((resourceLocation, block) -> dropSelf(block)));
    }
}
