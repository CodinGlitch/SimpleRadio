package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.core.registry.SimpleRadioModels;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class MixinModelBakery {

    @Shadow public abstract UnbakedModel getModel(ResourceLocation p_119342_);

    @Shadow @Final private Map<ResourceLocation, UnbakedModel> unbakedCache;

    @Shadow @Final private Map<ModelResourceLocation, UnbakedModel> topLevelModels;

    @Inject(at = @At(value = "TAIL"), method = "<init>")
    private void simpleradio$init_registerModel(BlockColors $$0, ProfilerFiller $$1, Map $$2, Map $$3, CallbackInfo ci) {
        List<ModelResourceLocation> locations = new ArrayList<>();
        SimpleRadioModels.onModelsRegister(locations::add);

        for (ModelResourceLocation location : locations) {
            UnbakedModel unbakedmodel = this.getModel(location.id());
            unbakedmodel.resolveParents(this::getModel);
            this.unbakedCache.put(location.id(), unbakedmodel);
            this.topLevelModels.put(location, unbakedmodel);
        }
    }
}