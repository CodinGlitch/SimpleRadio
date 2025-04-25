package com.codinglitch.simpleradio.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class SimpleRadioDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        generator.addProvider(SimpleRadioLootTableProvider::new);
        generator.addProvider(SimpleRadioRecipeProvider::new);
    }
}
