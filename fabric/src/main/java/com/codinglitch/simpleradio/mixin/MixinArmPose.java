package com.codinglitch.simpleradio.mixin;

import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HumanoidModel.ArmPose.class)
public enum MixinArmPose {
    SIMPLE_RADIO_HOLD_LAPEL(false);

    @Shadow
    MixinArmPose(boolean twoHanded) {
    }
}