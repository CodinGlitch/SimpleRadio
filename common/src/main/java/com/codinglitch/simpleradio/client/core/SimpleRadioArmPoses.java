package com.codinglitch.simpleradio.client.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.compat.AccessoryCompat;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SimpleRadioArmPoses {

    public static final Map<String, Pose> POSES = new HashMap<>();

    public record Pose(boolean twoHanded, Transform transform) {}

    @FunctionalInterface
    public interface Transform {
        void apply(HumanoidModel<?> model, LivingEntity livingEntity, HumanoidArm arm);
    }

    public static final Pose HOLD_LAPEL = makePose(
            "SIMPLE_RADIO_HOLD_LAPEL",
            false,
            (model, livingEntity, arm) -> {
                if (arm == HumanoidArm.RIGHT) {
                    model.leftArm.xRot = (float) -(Math.PI * 0.5f);
                    model.leftArm.yRot = (float) -(Math.PI * 0.33f);
                } else {
                    model.leftArm.xRot = (float) -(Math.PI * 0.5f);
                    model.leftArm.yRot = (float) (Math.PI * 0.33f);
                }
            }
    );

    private static Pose makePose(String name, boolean twoHanded, Transform transform) {
        Pose pose = new Pose(twoHanded, transform);
        POSES.put(name, pose);
        return pose;
    }

    public static boolean poseLeftArm(HumanoidModel<?> model, LivingEntity livingEntity, HumanoidModel.ArmPose pose) {
        for (Map.Entry<String, Pose> entry : POSES.entrySet()) {
            if (entry.getKey().equals(pose.name())) {
                entry.getValue().transform.apply(model, livingEntity, HumanoidArm.LEFT);
                return true;
            }
        }

        return false;
    }

    public static boolean poseRightArm(HumanoidModel<?> model, LivingEntity livingEntity, HumanoidModel.ArmPose pose) {
        for (Map.Entry<String, Pose> entry : POSES.entrySet()) {
            if (entry.getKey().equals(pose.name())) {
                entry.getValue().transform.apply(model, livingEntity, HumanoidArm.RIGHT);
                return true;
            }
        }

        return false;
    }

    @Nullable
    public static HumanoidModel.ArmPose getPose(AbstractClientPlayer player, InteractionHand hand) {
        if (hand != InteractionHand.OFF_HAND) return null;

        ItemStack stack = AccessoryCompat.getAccessory(player, test -> test.getItem() instanceof TransceiverItem);
        if (stack.isEmpty()) return null;
        if (!stack.hasTag()) return null;
        if (!stack.getTag().contains("using")) return null;

        CommonSimpleRadio.info(HumanoidModel.ArmPose.values());
        return stack.getTag().getBoolean("using") ? HumanoidModel.ArmPose.valueOf("SIMPLE_RADIO_HOLD_LAPEL") : null;
    }
}
