package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.core.SimpleRadioArmPoses;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class MixinPlayerRenderer {
    @Inject(at = @At(value = "RETURN"), method = "getArmPose", cancellable = true)
    private static void simpleradio$getArmPose_handheldPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        HumanoidModel.ArmPose pose = SimpleRadioArmPoses.getPose(player, hand);
        if (pose == null) return;
        cir.setReturnValue(pose);
    }
}