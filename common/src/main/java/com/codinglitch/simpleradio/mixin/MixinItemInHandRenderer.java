package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.renderers.WireRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    @Inject(at = @At("HEAD"), method = "renderHandsWithItems")
    private void simpleradio$renderItem_renderWire(float delta, PoseStack poseStack, MultiBufferSource.BufferSource multiBufferSource, LocalPlayer localPlayer, int $$4, CallbackInfo ci) {
        WireRenderer.renderPlayer(localPlayer, multiBufferSource, poseStack, Minecraft.getInstance().getDeltaFrameTime());
    }
}