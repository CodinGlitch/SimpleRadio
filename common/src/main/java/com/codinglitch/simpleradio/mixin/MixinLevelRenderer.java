package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.core.registry.renderers.WireRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {

    @Shadow @Final private RenderBuffers renderBuffers;

    @Shadow @Final private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "renderLevel")
    private void simpleradio$renderLevel_renderWire(PoseStack poseStack, float partialTick, long $$2, boolean $$3, Camera camera, GameRenderer $$5, LightTexture $$6, Matrix4f $$7, CallbackInfo ci) {
        WireRenderer.renderPlayer(minecraft.player, this.renderBuffers.bufferSource(), poseStack, partialTick, camera);
    }
}