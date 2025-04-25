package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.FabricLoader;
import com.codinglitch.simpleradio.core.central.ItemHolder;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SimpleRadioRecipeProvider extends FabricRecipeProvider {

    public static final HashMap<FinishedRecipe, ResourceLocation> MAP = new HashMap<>();

    public SimpleRadioRecipeProvider(FabricDataGenerator generator) {
        super(generator);
    }

    protected Consumer<FinishedRecipe> withItemConditions(Consumer<FinishedRecipe> exporter, Item item) {
        Optional<Map.Entry<ResourceLocation, ItemHolder<Item>>> optional = SimpleRadioItems.ITEMS.entrySet().stream().filter(entry -> entry.getValue().get() == item).findFirst();
        if (optional.isEmpty())
            return exporter;

        ResourceLocation location = optional.get().getKey();
        Consumer<FinishedRecipe> output = withConditions(exporter, FabricLoader.itemsEnabled(location.getPath()));

        return recipe -> {
            MAP.put(recipe, location);
            output.accept(recipe);
        };
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> consumer) {
        CommonRecipeProvider.defineRecipes(item -> withItemConditions(consumer, item));
    }
}
