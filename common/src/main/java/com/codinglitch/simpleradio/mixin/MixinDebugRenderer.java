package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.radio.RadioRouter;
import com.codinglitch.simpleradio.routers.Router;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.joml.Vector3f;
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
            ClientRadioManager.renderDebug(poseStack, bufferSource, camera);
        }
    }
}