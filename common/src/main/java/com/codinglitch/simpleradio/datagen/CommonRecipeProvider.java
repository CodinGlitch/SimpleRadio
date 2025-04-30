package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommonRecipeProvider extends RecipeProvider {

    public static final HashMap<FinishedRecipe, ResourceLocation> MAP = new HashMap<>();

    public CommonRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    public void run(CachedOutput output) {
        super.run(output);
    }

    public static void defineRecipes(Function<Item, Consumer<FinishedRecipe>> conditionBuilder) {
        ShapedRecipeBuilder.shaped(SimpleRadioItems.TRANSCEIVER)
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
                .unlockedBy("has_transmitting_module", doesHave(SimpleRadioItems.TRANSMITTING_MODULE))
                .unlockedBy("has_speaker_module", doesHave(SimpleRadioItems.SPEAKER_MODULE))
                .unlockedBy("has_receiving_module", doesHave(SimpleRadioItems.RECEIVING_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSCEIVER));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.WALKIE_TALKIE)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.COPPER_BLOCK)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .pattern(" I ")
                .pattern(" B ")
                .pattern(" C ")
                .unlockedBy("has_iron_ingot", doesHave(Items.IRON_INGOT))
                .unlockedBy("has_copper_block", doesHave(Items.COPPER_BLOCK))
                .unlockedBy("has_copper_wire", doesHave(SimpleRadioItems.COPPER_WIRE))
                .save(conditionBuilder.apply(SimpleRadioItems.WALKIE_TALKIE));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.SPUDDIE_TALKIE)
                .define('I', Items.IRON_INGOT)
                .define('P', Items.POTATO)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .pattern(" I ")
                .pattern(" P ")
                .pattern(" C ")
                .unlockedBy("has_iron_ingot", doesHave(Items.IRON_INGOT))
                .unlockedBy("has_potato", doesHave(Items.POTATO))
                .unlockedBy("has_copper_wire", doesHave(SimpleRadioItems.COPPER_WIRE))
                .save(conditionBuilder.apply(SimpleRadioItems.SPUDDIE_TALKIE));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.COPPER_WIRE, 2)
                .define('C', Items.COPPER_INGOT)
                .pattern(" C ")
                .pattern("C C")
                .pattern(" C ")
                .unlockedBy("has_copper_ingot", doesHave(Items.COPPER_INGOT))
                .save(conditionBuilder.apply(SimpleRadioItems.COPPER_WIRE));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.INSULATOR)
                .define('I', Items.IRON_INGOT)
                .define('P', ItemTags.PLANKS)
                .pattern("PIP")
                .unlockedBy("has_iron_ingot", doesHave(Items.IRON_INGOT))
                .unlockedBy("has_planks", doesHave(ItemTags.PLANKS))
                .unlockedBy("has_copper_wire", doesHave(SimpleRadioItems.COPPER_WIRE))
                .save(conditionBuilder.apply(SimpleRadioItems.INSULATOR));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.RADIO)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .define('Z', SimpleRadioItems.RECEIVING_MODULE)
                .pattern(" ZA")
                .pattern("RIC")
                .pattern(" Y ")
                .unlockedBy("has_speaker_module", doesHave(SimpleRadioItems.SPEAKER_MODULE))
                .unlockedBy("has_receiving_module", doesHave(SimpleRadioItems.RECEIVING_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.RADIO));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.SPEAKER)
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('Y', SimpleRadioItems.SPEAKER_MODULE)
                .pattern(" Y ")
                .pattern("ICI")
                .pattern("RI ")
                .unlockedBy("has_speaker_module", doesHave(SimpleRadioItems.SPEAKER_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.SPEAKER));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.MICROPHONE)
                .define('I', Items.IRON_INGOT)
                .define('W', ItemTags.WOOL)
                .define('R', Items.REDSTONE)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('L', SimpleRadioItems.LISTENER_MODULE)
                .pattern(" W ")
                .pattern("RLC")
                .pattern(" I ")
                .unlockedBy("has_listener_module", doesHave(SimpleRadioItems.LISTENER_MODULE))
                .save(conditionBuilder.apply(SimpleRadioItems.MICROPHONE));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.RECEIVER)
                .define('B', Items.IRON_BLOCK)
                .define('Q', Items.QUARTZ)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('R', SimpleRadioItems.RECEIVING_MODULE)
                .pattern(" A ")
                .pattern("QBR")
                .pattern(" C ")
                .unlockedBy("has_receiving_module", doesHave(SimpleRadioItems.RECEIVING_MODULE))
                .unlockedBy("has_antenna", doesHave(SimpleRadioItems.ANTENNA))
                .save(conditionBuilder.apply(SimpleRadioItems.RECEIVER));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.TRANSMITTER)
                .define('B', Items.IRON_BLOCK)
                .define('Q', Items.QUARTZ)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('T', SimpleRadioItems.TRANSMITTING_MODULE)
                .pattern(" A ")
                .pattern("QBT")
                .pattern(" C ")
                .unlockedBy("has_transmitting_module", doesHave(SimpleRadioItems.TRANSMITTING_MODULE))
                .unlockedBy("has_antenna", doesHave(SimpleRadioItems.ANTENNA))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSMITTER));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.ANTENNA)
                .define('I', Items.IRON_INGOT)
                .define('B', Items.IRON_BARS)
                .pattern(" B ")
                .pattern(" B ")
                .pattern(" I ")
                .unlockedBy("has_iron_ingot", doesHave(Items.IRON_INGOT))
                .unlockedBy("has_iron_bars", doesHave(Items.IRON_BARS))
                .save(conditionBuilder.apply(SimpleRadioItems.ANTENNA));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.RADIOSMITHER)
                .define('I', Items.IRON_INGOT)
                .define('A', Items.AMETHYST_SHARD)
                .define('D', Items.POLISHED_DEEPSLATE)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .pattern(" C ")
                .pattern("IAI")
                .pattern("DDD")
                .unlockedBy("has_iron_ingot", doesHave(Items.IRON_INGOT))
                .unlockedBy("has_polished_deepslate", doesHave(Items.POLISHED_DEEPSLATE))
                .unlockedBy("has_amethyst_shard", doesHave(Items.AMETHYST_SHARD))
                .save(conditionBuilder.apply(SimpleRadioItems.RADIOSMITHER));

        //---- Modules ----\\
        ShapedRecipeBuilder.shaped(SimpleRadioItems.TRANSMITTING_MODULE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('I', Items.IRON_INGOT)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('P', Items.ENDER_PEARL)
                .pattern(" A ")
                .pattern("CIC")
                .pattern(" P ")
                .unlockedBy("has_copper_wire", doesHave(SimpleRadioItems.COPPER_WIRE))
                .unlockedBy("has_ender_pearl", doesHave(Items.ENDER_PEARL))
                .save(conditionBuilder.apply(SimpleRadioItems.TRANSMITTING_MODULE));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.SPEAKER_MODULE)
                .define('I', Items.IRON_INGOT)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('N', Items.NOTE_BLOCK)
                .pattern(" N ")
                .pattern(" I ")
                .pattern(" C ")
                .unlockedBy("has_copper_wire", doesHave(SimpleRadioItems.COPPER_WIRE))
                .unlockedBy("has_note_block", doesHave(Items.NOTE_BLOCK))
                .save(conditionBuilder.apply(SimpleRadioItems.SPEAKER_MODULE));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.RECEIVING_MODULE)
                .define('A', SimpleRadioItems.ANTENNA)
                .define('I', Items.IRON_INGOT)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('M', Items.AMETHYST_SHARD)
                .pattern(" A ")
                .pattern("CIC")
                .pattern(" M ")
                .unlockedBy("has_copper_wire", doesHave(SimpleRadioItems.COPPER_WIRE))
                .unlockedBy("has_redstone", doesHave(Items.REDSTONE))
                .save(conditionBuilder.apply(SimpleRadioItems.RECEIVING_MODULE));

        ShapedRecipeBuilder.shaped(SimpleRadioItems.LISTENER_MODULE)
                .define('I', Items.IRON_INGOT)
                .define('C', SimpleRadioItems.COPPER_WIRE)
                .define('W', ItemTags.WOOL)
                .pattern("W")
                .pattern("I")
                .pattern("C")
                .unlockedBy("has_copper_wire", doesHave(SimpleRadioItems.COPPER_WIRE))
                .unlockedBy("has_wool", doesHave(ItemTags.WOOL))
                .save(conditionBuilder.apply(SimpleRadioItems.LISTENER_MODULE));
    }

    private static InventoryChangeTrigger.TriggerInstance doesHave(ItemLike itemLike) {
        return trigger(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(new ItemLike[]{itemLike}).build());
    }

    private static InventoryChangeTrigger.TriggerInstance doesHave(TagKey<Item> itemTag) {
        return trigger(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(itemTag).build());
    }

    private static InventoryChangeTrigger.TriggerInstance trigger(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates);
    }
}
