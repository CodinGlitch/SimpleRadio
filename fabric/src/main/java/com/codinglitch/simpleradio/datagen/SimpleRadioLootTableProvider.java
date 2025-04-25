package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

public class SimpleRadioLootTableProvider extends FabricBlockLootTableProvider {

    public SimpleRadioLootTableProvider(FabricDataGenerator generator) {
        super(generator);
    }

    @Override
    public void generateBlockLootTables() {
        //TODO: create way to differentiate self-dropping blocks
        SimpleRadioBlocks.BLOCKS.forEach(((resourceLocation, block) -> dropSelf(block)));
    }
}
