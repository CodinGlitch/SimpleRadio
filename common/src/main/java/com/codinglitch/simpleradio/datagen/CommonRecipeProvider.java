package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CommonRecipeProvider extends RecipeProvider {

    public static final HashMap<Recipe<?>, ResourceLocation> MAP = new HashMap<>();

    public CommonRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    public static void defineRecipes(Function<Item, RecipeOutput> conditionBuilder) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SimpleRadioItems.TRANSCEIVER)
                .define('I', Items.IRON_INGOT)
                .define('Q', Items.AMETHYST_SHARD)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('W', SimpleRadioItems.LISTENER_MODULE)
                .define('X', SimpleRadioItems.TRANSMITTING_MODULE)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .define('Z', SimpleRadioItems.RECEIVING_MODULE)
                .pattern("AWC")
                .pattern("XIZ")
                .pattern("QYQ")
                .unlockedBy("has_transmitting_module", has(SimpleRadioItems.TRANSMITTING_MODULE))
                .unlockedBy("has_speaker_module", has(SimpleRadioItems.SPEAKER_MODULE))
                .unlockedBy("has_receiving_module", has(SimpleRadioItems.RECEIVING_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSCEIVER));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SimpleRadioItems.WALKIE_TALKIE)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.COPPER_BLOCK)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .pattern(" I ")
                .pattern(" B ")
                .pattern(" C ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_copper_block", has(Items.COPPER_BLOCK))
                .unlockedBy("has_copper_wire", has(SimpleRadioItems.COPPER_WIRE))
                .save(conditionBuilder.apply(SimpleRadioItems.WALKIE_TALKIE));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, SimpleRadioItems.SPUDDIE_TALKIE)
                .define('I', Items.IRON_INGOT)
                .define('P', Items.POTATO)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .pattern(" I ")
                .pattern(" P ")
                .pattern(" C ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_potato", has(Items.POTATO))
                .unlockedBy("has_copper_wire", has(SimpleRadioItems.COPPER_WIRE))
                .save(conditionBuilder.apply(SimpleRadioItems.SPUDDIE_TALKIE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.COPPER_WIRE, 2)
                .define('C', Items.COPPER_INGOT)
                .pattern(" C ")
                .pattern("C C")
                .pattern(" C ")
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(conditionBuilder.apply(SimpleRadioItems.COPPER_WIRE));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.INSULATOR)
                .define('I', Items.IRON_INGOT)
                .define('P', ItemTags.PLANKS)
                .pattern("PIP")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .unlockedBy("has_copper_wire", has(SimpleRadioItems.COPPER_WIRE))
                .save(conditionBuilder.apply(SimpleRadioItems.INSULATOR));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.RADIO)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .define('Z', SimpleRadioItems.RECEIVING_MODULE)
                .pattern(" ZA")
                .pattern("RIC")
                .pattern(" Y ")
                .unlockedBy("has_speaker_module", has(SimpleRadioItems.SPEAKER_MODULE))
                .unlockedBy("has_receiving_module", has(SimpleRadioItems.RECEIVING_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.RADIO));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.SPEAKER)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .pattern(" Y ")
                .pattern("ICI")
                .pattern("RI ")
                .unlockedBy("has_speaker_module", has(SimpleRadioItems.SPEAKER_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.SPEAKER));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.MICROPHONE)
                .define('I', Items.IRON_INGOT)
                .define('W', ItemTags.WOOL)
                .define('R', Items.REDSTONE)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('L', SimpleRadioItems.LISTENER_MODULE)
                .pattern(" W ")
                .pattern("RLC")
                .pattern(" I ")
                .unlockedBy("has_listener_module", has(SimpleRadioItems.LISTENER_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.MICROPHONE));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.RECEIVER)
                .define('B', Items.IRON_BLOCK)
                .define('Q', Items.QUARTZ)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('R', SimpleRadioItems.RECEIVING_MODULE)
                .pattern(" A ")
                .pattern("QBR")
                .pattern(" C ")
                .unlockedBy("has_receiving_module", has(SimpleRadioItems.RECEIVING_MODULE))
                .unlockedBy("has_antenna", has(SimpleRadioItems.ANTENNA))
                .save(conditionBuilder.apply(SimpleRadioItems.RECEIVER));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.TRANSMITTER)
                .define('B', Items.IRON_BLOCK)
                .define('Q', Items.QUARTZ)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('T', SimpleRadioItems.TRANSMITTING_MODULE)
                .pattern(" A ")
                .pattern("QBT")
                .pattern(" C ")
                .unlockedBy("has_transmitting_module", has(SimpleRadioItems.TRANSMITTING_MODULE))
                .unlockedBy("has_antenna", has(SimpleRadioItems.ANTENNA))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSMITTER));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.ANTENNA)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.IRON_BARS)
                .pattern(" B ")
                .pattern(" B ")
                .pattern(" I ")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(conditionBuilder.apply(SimpleRadioItems.ANTENNA));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, SimpleRadioItems.RADIOSMITHER)
                .define('I', Items.IRON_INGOT)
                .define('A', Items.AMETHYST_SHARD)
                .define('D', Items.POLISHED_DEEPSLATE)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .pattern(" C ")
                .pattern("IAI")
                .pattern("DDD")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .unlockedBy("has_polished_deepslate", has(Items.POLISHED_DEEPSLATE))
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(conditionBuilder.apply(SimpleRadioItems.RADIOSMITHER));

        //---- Modules ----\\
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.TRANSMITTING_MODULE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('I', Items.IRON_INGOT)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('P', Items.ENDER_PEARL)
                .pattern(" A ")
                .pattern("CIC")
                .pattern(" P ")
                .unlockedBy("has_copper_wire", has(SimpleRadioItems.COPPER_WIRE))
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSMITTING_MODULE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.SPEAKER_MODULE)
                .define('I', Items.IRON_INGOT)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('N', Items.NOTE_BLOCK)
                .pattern(" N ")
                .pattern(" I ")
                .pattern(" C ")
                .unlockedBy("has_copper_wire", has(SimpleRadioItems.COPPER_WIRE))
                .unlockedBy("has_note_block", has(Items.NOTE_BLOCK))
                .save(conditionBuilder.apply(SimpleRadioItems.SPEAKER_MODULE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.RECEIVING_MODULE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('I', Items.IRON_INGOT)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('M', Items.AMETHYST_SHARD)
                .pattern(" A ")
                .pattern("CIC")
                .pattern(" M ")
                .unlockedBy("has_copper_wire", has(SimpleRadioItems.COPPER_WIRE))
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(conditionBuilder.apply(SimpleRadioItems.RECEIVING_MODULE));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SimpleRadioItems.LISTENER_MODULE)
                .define('I', Items.IRON_INGOT)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('W', ItemTags.WOOL)
                .pattern("W")
                .pattern("I")
                .pattern("C")
                .unlockedBy("has_copper_wire", has(SimpleRadioItems.COPPER_WIRE))
                .unlockedBy("has_wool", has(ItemTags.WOOL))
                .save(conditionBuilder.apply(SimpleRadioItems.LISTENER_MODULE));
    }

    @Override
    public void buildRecipes(RecipeOutput output) {

    }
}
