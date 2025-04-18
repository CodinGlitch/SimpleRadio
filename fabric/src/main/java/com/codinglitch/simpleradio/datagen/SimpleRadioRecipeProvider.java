package com.codinglitch.simpleradio.datagen;

import com.codinglitch.simpleradio.core.FabricLoader;
import com.codinglitch.simpleradio.core.central.ItemHolder;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SimpleRadioRecipeProvider extends FabricRecipeProvider {

    public static final HashMap<Recipe<?>, ResourceLocation> MAP = new HashMap<>();

    public SimpleRadioRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    protected RecipeOutput withItemConditions(RecipeOutput exporter, Item item) {
        Optional<Map.Entry<ResourceLocation, ItemHolder<Item>>> optional = SimpleRadioItems.ITEMS.entrySet().stream().filter(entry -> entry.getValue().get() == item).findFirst();
        if (optional.isEmpty())
            return exporter;

        ResourceLocation location = optional.get().getKey();
        RecipeOutput output = withConditions(exporter, FabricLoader.itemsEnabled(location.getPath()));

        return new RecipeOutput() {

            @Override
            public void accept(ResourceLocation resourceLocation, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder) {
                MAP.put(recipe, location);
                output.accept(resourceLocation, recipe, advancementHolder);
            }

            @Override
            public Advancement.Builder advancement() {
                return null;
            }
        };
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        CommonRecipeProvider.defineRecipes(item -> withItemConditions(output, item));
    }
}
