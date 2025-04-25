package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.core.registry.SimpleRadioModels;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelManager.class)
public abstract class MixinModelManager implements PreparableReloadListener, AutoCloseable {

    @Shadow public abstract BakedModel getModel(ModelResourceLocation modelResourceLocation);

    @Shadow private Map<ResourceLocation, BakedModel> bakedRegistry;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"
            ), method = "apply(Lnet/minecraft/client/resources/model/ModelBakery;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"
    )
    private void simpleradio$apply_loadModels(ModelBakery bakery, ResourceManager resourceManager, ProfilerFiller filler, CallbackInfo ci) {
        SimpleRadioModels.onModelsLoad(this.bakedRegistry);
    }
}