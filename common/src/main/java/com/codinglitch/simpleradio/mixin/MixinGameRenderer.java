package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.renderers.WireRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow public abstract Minecraft getMinecraft();

    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(at = @At("HEAD"), method = "renderItemInHand")
    private void simpleradio$renderItemInHand_renderWire(PoseStack poseStack, Camera camera, float $$2, CallbackInfo ci) {
        WireRenderer.renderPlayerHeld(this.getMinecraft().player, this.renderBuffers.bufferSource(), poseStack, Minecraft.getInstance().getDeltaFrameTime());
    }
}