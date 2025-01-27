package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommonRecipeProvider extends RecipeProvider {

    public static final HashMap<FinishedRecipe, ResourceLocation> MAP = new HashMap<>();

    public CommonRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {}

    public static void defineRecipes(Function<Item, Consumer<FinishedRecipe>> conditionBuilder) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SimpleRadioItems.TRANSCEIVER)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('X', SimpleRadioItems.TRANSMITTING_MODULE)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .define('Z', SimpleRadioItems.RECEIVING_MODULE)
                .pattern("AII")
                .pattern("XRZ")
                .pattern("IYI")
                .unlockedBy("has_transmitting_module", has(SimpleRadioItems.TRANSMITTING_MODULE))
                .unlockedBy("has_speaker_module", has(SimpleRadioItems.SPEAKER_MODULE))
                .unlockedBy("has_receiving_module", has(SimpleRadioItems.RECEIVING_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSCEIVER));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SimpleRadioItems.WALKIE_TALKIE)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.COPPER_BLOCK)
                .define('C', Items.COPPER_INGOT)
                .pattern(" I ")
                .pattern(" B ")
                .pattern(" C ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_copper_block", has(Items.COPPER_BLOCK))
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(conditionBuilder.apply(SimpleRadioItems.WALKIE_TALKIE));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SimpleRadioItems.SPUDDIE_TALKIE)
                .define('I', Items.IRON_INGOT)
                .define('P', Items.POTATO)
                .define('C', Items.COPPER_INGOT)
                .pattern(" I ")
                .pattern(" P ")
                .pattern(" C ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_potato", has(Items.POTATO))
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(conditionBuilder.apply(SimpleRadioItems.SPUDDIE_TALKIE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.COPPER_WIRE, 2)
                .define('C', Items.COPPER_INGOT)
                .pattern(" C ")
                .pattern("C C")
                .pattern(" C ")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(conditionBuilder.apply(SimpleRadioItems.COPPER_WIRE));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.SOCKET)
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .define('C', Items.COPPER_INGOT)
                .pattern("NCN")
                .pattern(" I ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(conditionBuilder.apply(SimpleRadioItems.SOCKET));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.RADIO)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .define('Z', SimpleRadioItems.RECEIVING_MODULE)
                .pattern("IIA")
                .pattern("ZYR")
                .pattern("III")
                .unlockedBy("has_speaker_module", has(SimpleRadioItems.SPEAKER_MODULE))
                .unlockedBy("has_receiving_module", has(SimpleRadioItems.RECEIVING_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.RADIO));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.SPEAKER)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .pattern("III")
                .pattern("IRY")
                .pattern("III")
                .unlockedBy("has_speaker_module", has(SimpleRadioItems.SPEAKER_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.SPEAKER));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.MICROPHONE)
                .define('I', Items.IRON_INGOT)
                .define('W', ItemTags.WOOL)
                .define('L', SimpleRadioItems.LISTENER_MODULE)
                .pattern(" W ")
                .pattern(" L ")
                .pattern("I I")
                .unlockedBy("has_listener_module", has(SimpleRadioItems.LISTENER_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.MICROPHONE));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.RECEIVER)
                .define('B', Items.IRON_BLOCK)
                .define('G', Items.GOLD_INGOT)
                .define('W', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('R', SimpleRadioItems.RECEIVING_MODULE)
                .pattern(" A ")
                .pattern("GBW")
                .pattern(" R ")
                .unlockedBy("has_listener_module", has(SimpleRadioItems.RECEIVER))
                .save(conditionBuilder.apply(SimpleRadioItems.RECEIVER));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.TRANSMITTER)
                .define('B', Items.IRON_BLOCK)
                .define('G', Items.GOLD_INGOT)
                .define('W', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('T', SimpleRadioItems.TRANSMITTING_MODULE)
                .pattern(" A ")
                .pattern("GBW")
                .pattern(" T ")
                .unlockedBy("has_listener_module", has(SimpleRadioItems.TRANSMITTING_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSMITTER));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.ANTENNA)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.IRON_BARS)
                .pattern(" B ")
                .pattern(" I ")
                .pattern(" B ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(conditionBuilder.apply(SimpleRadioItems.ANTENNA));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.RADIOSMITHER)
                .define('I', Items.IRON_INGOT)
                .define('A', Items.AMETHYST_SHARD)
                .define('D', Items.POLISHED_DEEPSLATE)
                .pattern("IAI")
                .pattern("DDD")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_polished_deepslate", has(Items.POLISHED_DEEPSLATE))
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(conditionBuilder.apply(SimpleRadioItems.RADIOSMITHER));

        //---- Modules ----\\
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.TRANSMITTING_MODULE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('C', Items.COPPER_INGOT)
                .define('L', Items.LAPIS_LAZULI)
                .pattern("A")
                .pattern("C")
                .pattern("L")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_lapis_lazuli", has(Items.LAPIS_LAZULI))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSMITTING_MODULE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.SPEAKER_MODULE)
                .define('R', Items.REDSTONE)
                .define('C', Items.COPPER_INGOT)
                .define('N', Items.NOTE_BLOCK)
                .pattern("R")
                .pattern("C")
                .pattern("N")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_note_block", has(Items.NOTE_BLOCK))
                .save(conditionBuilder.apply(SimpleRadioItems.SPEAKER_MODULE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.RECEIVING_MODULE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('C', Items.COPPER_INGOT)
                .define('M', Items.AMETHYST_SHARD)
                .pattern("A")
                .pattern("C")
                .pattern("M")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(conditionBuilder.apply(SimpleRadioItems.RECEIVING_MODULE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.LISTENER_MODULE)
                .define('R', Items.REDSTONE)
                .define('C', Items.COPPER_INGOT)
                .define('W', ItemTags.WOOL)
                .pattern("R")
                .pattern("C")
                .pattern("W")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .unlockedBy("has_wool", has(ItemTags.WOOL))
                .save(conditionBuilder.apply(SimpleRadioItems.LISTENER_MODULE));
    }
}
