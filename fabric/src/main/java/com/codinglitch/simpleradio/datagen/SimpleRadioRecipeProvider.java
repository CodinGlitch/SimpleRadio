package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public class SimpleRadioRecipeProvider extends FabricRecipeProvider {

    public SimpleRadioRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SimpleRadioItems.TRANSCEIVER)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('T', Items.REDSTONE_TORCH)
                .define('X', SimpleRadioItems.TRANSMITTING_MODULE)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .define('Z', SimpleRadioItems.RECEIVING_MODULE)
                .pattern("TII")
                .pattern("XRZ")
                .pattern("IYI")
                .unlockedBy("has_transmitting_module", has(SimpleRadioItems.TRANSMITTING_MODULE))
                .unlockedBy("has_speaker_module", has(SimpleRadioItems.SPEAKER_MODULE))
                .unlockedBy("has_receiving_module", has(SimpleRadioItems.RECEIVING_MODULE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.RADIO)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('T', Items.REDSTONE_TORCH)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .define('Z', SimpleRadioItems.RECEIVING_MODULE)
                .pattern("IIT")
                .pattern("ZYR")
                .pattern("III")
                .unlockedBy("has_speaker_module", has(SimpleRadioItems.SPEAKER_MODULE))
                .unlockedBy("has_receiving_module", has(SimpleRadioItems.RECEIVING_MODULE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.RADIOSMITHER)
                .define('I', Items.IRON_INGOT)
                .define('A', Items.AMETHYST_SHARD)
                .define('D', Items.POLISHED_DEEPSLATE)
                .pattern("IAI")
                .pattern("DDD")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_polished_deepslate", has(Items.POLISHED_DEEPSLATE))
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        //---- Modules ----\\
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.TRANSMITTING_MODULE)
                .define('R', Items.REDSTONE_TORCH)
                .define('I', Items.IRON_INGOT)
                .define('L', Items.LAPIS_LAZULI)
                .pattern("R")
                .pattern("I")
                .pattern("L")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_lapis_lazuli", has(Items.LAPIS_LAZULI))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.SPEAKER_MODULE)
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .define('N', Items.NOTE_BLOCK)
                .pattern("R")
                .pattern("I")
                .pattern("N")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_note_block", has(Items.NOTE_BLOCK))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.RECEIVING_MODULE)
                .define('R', Items.REDSTONE_TORCH)
                .define('I', Items.IRON_INGOT)
                .define('A', Items.AMETHYST_SHARD)
                .pattern("R")
                .pattern("I")
                .pattern("A")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);
    }
}
