package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.ItemsEnabledCondition;
import com.codinglitch.simpleradio.core.central.ItemHolder;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.google.gson.JsonElement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SimpleRadioRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public SimpleRadioRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> existingFileHelper) {
        super(output, existingFileHelper);
    }

    protected RecipeOutput withItemConditions(RecipeOutput exporter, Item item) {
        Optional<Map.Entry<ResourceLocation, ItemHolder<Item>>> optional = SimpleRadioItems.ITEMS.entrySet().stream().filter(entry -> entry.getValue().get() == item).findFirst();
        if (optional.isEmpty())
            return exporter;

        ResourceLocation location = optional.get().getKey();
        return exporter.withConditions(new ItemsEnabledCondition(location.getPath()));
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        CommonRecipeProvider.defineRecipes(item -> withItemConditions(output, item));
    }
}
