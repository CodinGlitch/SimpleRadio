package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.renderers.WireRenderer;
import com.codinglitch.simpleradio.core.central.AuditoryBlockEntity;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.blocks.FrequencerBlockEntity;
import com.codinglitch.simpleradio.core.registry.items.WireItem;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.system.MathUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerRenderer.class)
public class MixinPlayerRenderer {

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void simpleradio$renderPlayer(AbstractClientPlayer player, float f, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo ci) {
        WireRenderer.renderPlayer(player, multiBufferSource, poseStack, partialTick);
    }
}