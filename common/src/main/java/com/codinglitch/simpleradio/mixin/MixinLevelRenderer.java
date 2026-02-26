package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.core.registry.renderers.WireRenderer;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"), method = "renderLevel")
    private void simpleradio$renderLevel_renderWire(DeltaTracker deltaTracker, boolean $$1, Camera camera, GameRenderer renderer, LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f1, CallbackInfo ci, @Local PoseStack poseStack) {
        WireRenderer.renderPlayer(minecraft.player, this.renderBuffers.bufferSource(), poseStack, deltaTracker.getGameTimeDeltaTicks(), camera);
    }
}