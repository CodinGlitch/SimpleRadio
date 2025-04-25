package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.core.registry.SimpleRadioModels;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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
import java.util.Set;

@Mixin(ModelBakery.class)
public abstract class MixinModelBakery {

    @Shadow public abstract UnbakedModel getModel(ResourceLocation p_119342_);

    @Shadow @Final private Map<ResourceLocation, UnbakedModel> unbakedCache;

    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;

    @Inject(at = @At(value = "TAIL"), method = "<init>")
    private void simpleradio$init_registerModel(ResourceManager manager, BlockColors colors, ProfilerFiller $$2, int $$3, CallbackInfo ci) {
        List<ModelResourceLocation> locations = new ArrayList<>();
        SimpleRadioModels.onModelsRegister(locations::add);

        Set<Pair<String, String>> set = Sets.newLinkedHashSet();
        for (ModelResourceLocation location : locations) {
            UnbakedModel unbakedmodel = this.getModel(location);
            unbakedmodel.getMaterials(this::getModel, set);
            this.unbakedCache.put(location, unbakedmodel);
            this.topLevelModels.put(location, unbakedmodel);
        }
    }
}