package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.ItemsEnabledCondition;
import com.codinglitch.simpleradio.core.central.ItemHolder;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.google.gson.JsonElement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class SimpleRadioRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public SimpleRadioRecipeProvider(PackOutput output) {
        super(output);
    }

    protected RecipeOutput withItemConditions(RecipeOutput exporter, Item item) {
        Optional<Map.Entry<ResourceLocation, ItemHolder<Item>>> optional = SimpleRadioItems.ITEMS.entrySet().stream().filter(entry -> entry.getValue().get() == item).findFirst();
        if (optional.isEmpty())
            return exporter;

        ResourceLocation location = optional.get().getKey();

        return new RecipeOutput() {

            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder) {
                ConditionalRecipe.builder()
                        .condition(new ItemsEnabledCondition(location.getPath()))
                        .recipe(id, recipe, advancementHolder)
                        .save(exporter, location);
            }

            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable ResourceLocation advancementId, @Nullable JsonElement advancement) {
                ConditionalRecipe.builder()
                        .condition(new ItemsEnabledCondition(location.getPath()))
                        .recipe(exporter -> exporter.accept(id, recipe, advancementId, advancement))
                        .save(exporter, location);
            }

            @Override
            public Advancement.Builder advancement() {
                return exporter.advancement();
            }
        };
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        CommonRecipeProvider.defineRecipes(item -> withItemConditions(output, item));
    }
}
