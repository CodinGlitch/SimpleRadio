package com.codinglitch.simpleradio.client.core;

import com.codinglitch.simpleradio.core.registry.blocks.RadioBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Function;

public abstract class AnimatableModel extends Model {
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

    protected void animate(AnimationState animationState, AnimationDefinition animationDefinition, float age, float speed) {
        animationState.updateTime(age, speed);
        animationState.ifStarted((p_233392_) -> {
            ModelAnimations.animate(this, animationDefinition, p_233392_.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE);
        });
    }

    public abstract void setupAnim(RadioBlockEntity blockEntity, float ageInTicks);

    @Override
    public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, float v, float v1, float v2, float v3);
}
