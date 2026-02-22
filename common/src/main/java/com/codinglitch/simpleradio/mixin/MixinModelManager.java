package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.core.registry.SimpleRadioModels;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
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
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"
            ), method = "apply(Lnet/minecraft/client/resources/model/ModelManager$ReloadState;Lnet/minecraft/util/profiling/ProfilerFiller;)V"
    )
    private void simpleradio$apply_loadModels(ModelManager.ReloadState state, ProfilerFiller filler, CallbackInfo ci) {
        SimpleRadioModels.onModelsLoad(this.bakedRegistry);
    }
}