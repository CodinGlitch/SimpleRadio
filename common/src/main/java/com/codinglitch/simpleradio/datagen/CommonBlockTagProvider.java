package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommonBlockTagProvider extends TagsProvider<Block> {

    @FunctionalInterface
    public interface TagAdder {
        void add(Block... items);
    }

    protected CommonBlockTagProvider(PackOutput output, ResourceKey<? extends Registry<Block>> registryKey, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, registryKey, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {}

    public static void defineTags(Function<TagKey<Block>, TagAdder> builder) {
        builder.apply(BlockTags.NEEDS_STONE_TOOL)
                .add(
                        SimpleRadioBlocks.ANTENNA,
                        SimpleRadioBlocks.RADIO,
                        SimpleRadioBlocks.FREQUENCER,
                        SimpleRadioBlocks.RADIOSMITHER,

                        SimpleRadioBlocks.RECEIVER,
                        SimpleRadioBlocks.TRANSMITTER,

                        SimpleRadioBlocks.SPEAKER,
                        SimpleRadioBlocks.MICROPHONE
                );

        builder.apply(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        SimpleRadioBlocks.ANTENNA,
                        SimpleRadioBlocks.RADIO,
                        SimpleRadioBlocks.FREQUENCER,
                        SimpleRadioBlocks.RADIOSMITHER,

                        SimpleRadioBlocks.RECEIVER,
                        SimpleRadioBlocks.TRANSMITTER,

                        SimpleRadioBlocks.SPEAKER,
                        SimpleRadioBlocks.MICROPHONE
                );

        builder.apply(BlockTags.MINEABLE_WITH_AXE)
                .add(
                        SimpleRadioBlocks.INSULATOR
                );
    }
}
