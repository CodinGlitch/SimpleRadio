package com.codinglitch.simpleradio.client.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.core.registry.models.ModuleModel;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleRadioModels {
    public static final List<ModelResourceLocation> MODELS = new ArrayList<>();
    public static final List<ModelSubstitution<BakedModel>> SUBSTITUTIONS = new ArrayList<>();

    public static final List<ModelOverride> OVERRIDES = new ArrayList<>();

    public static ModelSubstitution<ModuleModel> MODULE = register(new ModelSubstitution<>(ModuleModel::new,
            new ModelResourceLocation(CommonSimpleRadio.id("iron_module"), "inventory"),
            new ModelResourceLocation(CommonSimpleRadio.id("gold_module"), "inventory"),
            new ModelResourceLocation(CommonSimpleRadio.id("diamond_module"), "inventory"),
            new ModelResourceLocation(CommonSimpleRadio.id( "netherite_module"), "inventory")
    ));

    public static ModelResourceLocation TRANSCEIVER = register(new ModelResourceLocation(CommonSimpleRadio.id("item/transceiver_item"), "inventory"));
    public static ModelResourceLocation TRANSCEIVER_IN_HAND = register(new ModelResourceLocation(CommonSimpleRadio.id("item/transceiver"), "inventory"));

    // ----

    public static ModelOverride register(ModelOverride override) {
        OVERRIDES.add(override);
        return override;
    }

    public static ModelResourceLocation register(ModelResourceLocation model) {
        MODELS.add(model);
        return model;
    }
    public static <M extends BakedModel> ModelSubstitution<M> register(ModelSubstitution<M> model) {
        SUBSTITUTIONS.add((ModelSubstitution<BakedModel>) model);
        return model;
    }

    public static void onModelsRegister(Consumer<ModelResourceLocation> registry) {
        for (ModelResourceLocation model : MODELS) {
            CommonSimpleRadio.info("Adding new model for {}", model);
            registry.accept(model);
        }
    }

    public static void onModelsLoad(Map<ResourceLocation, BakedModel> bakedRegistry) {
        for (ModelSubstitution<BakedModel> substitution : SUBSTITUTIONS) {
            for (ModelResourceLocation location : substitution.locations) {
                BakedModel existingModel = bakedRegistry.get(location);
                if (existingModel == null) {
                    CommonSimpleRadio.warn("Could not find model {}", location);
                } else {
                    CommonSimpleRadio.info("Replacing model for {}", location);

                    BakedModel newModel = substitution.substitutor.apply(existingModel);
                    if (newModel instanceof LocationHolder locationHolder)
                        locationHolder.location = location;

                    bakedRegistry.put(location.id(), newModel);
                }
            }
        }
    }

    public static BakedModel tryOverride(ItemDisplayContext context, ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity, int id, Function<ModelResourceLocation, BakedModel> retriever) {
        for (ModelOverride override : OVERRIDES) {
            if (!stack.is(override.item)) continue;

            boolean isInContext = false;
            for (ItemDisplayContext overrideContext : override.contexts) {
                if (overrideContext == context) {
                    isInContext = true;
                    break;
                }
            }
            if (!isInContext) continue;

            BakedModel newModel = retriever.apply(override.location);

            ClientLevel clientLevel = level instanceof ClientLevel ? (ClientLevel) level : null;
            return newModel.getOverrides().resolve(newModel, stack, clientLevel, entity, id);
        }

        return null;
    }

    public static void register() {

    }

    public static void load() {
        // ---- Overrides ---- \\

        register(new ModelOverride(
                List.of(ItemDisplayContext.NONE),
                TRANSCEIVER, SimpleRadioItems.TRANSCEIVER
        ));
    }

    public static class LocationHolder {
        public ModelResourceLocation location = null;
    }
    
    public static class ModelSubstitution<M extends BakedModel> {
        public final Function<BakedModel, M> substitutor;
        public final List<ModelResourceLocation> locations;

        public ModelSubstitution(Function<BakedModel, M> substitutor, ModelResourceLocation... locations) {
            this.substitutor = substitutor;
            this.locations = Arrays.stream(locations).toList();
        }
    }

    public record ModelOverride(List<ItemDisplayContext> contexts, ModelResourceLocation location, Item item) { }
}
