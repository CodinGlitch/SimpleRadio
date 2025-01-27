package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.ItemsEnabledCondition;
import com.codinglitch.simpleradio.core.central.ItemHolder;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SimpleRadioRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public SimpleRadioRecipeProvider(PackOutput output) {
        super(output);
    }

    protected Consumer<FinishedRecipe> withItemConditions(Consumer<FinishedRecipe> exporter, Item item) {
        Optional<Map.Entry<ResourceLocation, ItemHolder<Item>>> optional = SimpleRadioItems.ITEMS.entrySet().stream().filter(entry -> entry.getValue().get() == item).findFirst();
        if (optional.isEmpty())
            return exporter;

        ResourceLocation location = optional.get().getKey();

        return recipe -> {
            ConditionalRecipe.builder()
                    .addCondition(new ItemsEnabledCondition("microphone"))
                    .addRecipe(recipe)
                    .build(exporter, location);
        };
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        CommonRecipeProvider.defineRecipes(item -> withItemConditions(output, item));
    }
}
