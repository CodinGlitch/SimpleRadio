package com.codinglitch.simpleradio.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class SimpleRadioBlockTagProvider extends FabricTagProvider<Block> {

    public SimpleRadioBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.BLOCK, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        CommonBlockTagProvider.defineTags(key ->items -> {
            this.getOrCreateTagBuilder(key).add(items);
        });
    }
}
