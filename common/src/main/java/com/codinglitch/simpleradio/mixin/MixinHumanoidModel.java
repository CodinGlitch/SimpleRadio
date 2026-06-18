package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.core.SimpleRadioArmPoses;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class MixinHumanoidModel {
    @Shadow
    public HumanoidModel.ArmPose leftArmPose;

    @Shadow
    public HumanoidModel.ArmPose rightArmPose;

    @Inject(at = @At(value = "HEAD"), method = "poseLeftArm", cancellable = true)
    private void simpleradio$poseLeftArm(LivingEntity livingEntity, CallbackInfo ci) {
        if (SimpleRadioArmPoses.poseLeftArm((HumanoidModel<?>) (Object) this, livingEntity, this.leftArmPose)) ci.cancel();
    }

    @Inject(at = @At(value = "HEAD"), method = "poseRightArm", cancellable = true)
    private void simpleradio$poseRightArm(LivingEntity livingEntity, CallbackInfo ci) {
        if (SimpleRadioArmPoses.poseRightArm((HumanoidModel<?>) (Object) this, livingEntity, this.rightArmPose)) ci.cancel();
    }
}