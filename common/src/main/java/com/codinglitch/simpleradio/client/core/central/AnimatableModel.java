package com.codinglitch.simpleradio.client.core.central;

import com.codinglitch.simpleradio.core.central.Animatable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class AnimatableModel extends Model {
    protected final Map<Integer, AnimationDefinition> animations = new HashMap<>();
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public AnimatableModel(Function<ResourceLocation, RenderType> function) {
        super(function);
    }

    public abstract ModelPart root();

    public Optional<ModelPart> getAnyDescendantWithName(String name) {
        return name.equals("root") ? Optional.of(this.root()) : this.root().getAllParts().filter(part -> {
            return part.hasChild(name);
        }).findFirst().map((part) -> {
            return part.getChild(name);
        });
    }

    protected void animate(AnimationState animationState, AnimationDefinition animationDefinition, float age) {
        animationState.updateTime(age, 20f);
        animationState.ifStarted((state) -> {
            ModelAnimations.animate(this, animationDefinition, state.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE);
        });
    }

    public void setupAnim(Animatable animatable, float ageInTicks) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        for (Map.Entry<Integer, AnimationDefinition> entry : animations.entrySet()) {
            AnimationInstance instance = animatable.getAnim(entry.getKey());
            AnimationDefinition animation = entry.getValue();

            animate(instance, animation, ageInTicks);
        }
    }

    public void allocate(int id, AnimationDefinition definition) {
        animations.put(id, definition);
    }

    @Override
    public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color);
}
