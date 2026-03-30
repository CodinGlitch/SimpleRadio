package com.codinglitch.simpleradio.mixin;

import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StructureUtils.class, priority = 900)
public class MixinStructureUtils {
    @Inject(method = "getStructureTemplate", at = @At("HEAD"), cancellable = true)
    private static void simpleradio$useStructureManager(String name, ServerLevel level, CallbackInfoReturnable<StructureTemplate> cir) {
        level.getStructureManager().get(new ResourceLocation(name)).ifPresent(cir::setReturnValue);
    }
}
