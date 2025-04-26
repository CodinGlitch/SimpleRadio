package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.block.Block;

public class SimpleRadioBlockLootTableProvider extends BlockLoot {

    public SimpleRadioBlockLootTableProvider() {
        super();
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return SimpleRadioBlocks.BLOCKS.values();
    }

    @Override
    protected void addTables() {
        //TODO: create way to differentiate self-dropping blocks
        SimpleRadioBlocks.BLOCKS.forEach(((resourceLocation, block) -> dropSelf(block)));
    }
}
