package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.radio.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void simpleradio$postRender_renderRouter(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        Vector3f camera = new Vector3f((float) cameraX, (float) cameraY, (float) cameraZ);
        if (minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            for (RadioRouter router : ClientRadioManager.getRouters()) {
                ClientRadioManager.renderRouter(router, poseStack, bufferSource, camera);
            }
        }
    }
}