package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.core.registry.SimpleRadioModels;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow @Final private ItemModelShaper itemModelShaper;

    @WrapOperation(
            method = "getModel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemModelShaper;getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;")
    )
    private BakedModel simpleradio$getModel_modelOverride(
            ItemModelShaper instance,
            ItemStack stack,
            Operation<BakedModel> original,
            @Nullable @Local(argsOnly = true) Level level,
            @Nullable @Local(argsOnly = true) LivingEntity entity,
            @Local(argsOnly = true) int id
    ) {
        int ordinal = 0;
        if (entity != null) {
            ordinal = id - entity.getId();
        }

        ItemDisplayContext context = ItemDisplayContext.BY_ID.apply(ordinal);

        BakedModel newModel = SimpleRadioModels.tryOverride(context, stack, level, entity, id, instance.getModelManager()::getModel);

        if (newModel == null) return original.call(instance, stack);
        return newModel;
    }
}