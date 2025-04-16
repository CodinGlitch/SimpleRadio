package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.SimpleRadioModels;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
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

    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Inject(at = @At(value = "TAIL"), method = "<init>")
    private void simpleradio$init_registerModel(BlockColors $$0, ProfilerFiller $$1, Map $$2, Map $$3, CallbackInfo ci) {
        List<ModelResourceLocation> locations = new ArrayList<>();
        SimpleRadioModels.onModelsRegister(locations::add);

        for (ModelResourceLocation location : locations) {
            UnbakedModel unbakedmodel = this.getModel(location);
            unbakedmodel.resolveParents(this::getModel);
            this.unbakedCache.put(location, unbakedmodel);
            this.topLevelModels.put(location, unbakedmodel);
        }
    }
}