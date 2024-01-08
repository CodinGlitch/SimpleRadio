package com.codinglitch.simpleradio.client.models;


import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RadioModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(CommonSimpleRadio.id("radiomodel"), "main");
	public static final ResourceLocation TEXTURE_LOCATION = CommonSimpleRadio.id("textures/block/radio.png");
	private final ModelPart bone;

	public RadioModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(10, 11).addBox(-6.1003F, -3.005F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 11).addBox(4.8997F, -3.005F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(6, 11).addBox(-4.1003F, -5.005F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.1003F, -4.005F, -2.0F, 10.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, 0.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -3.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3997F, -4.005F, 0.0F, 0.0F, 0.1745F, 0.0F));
		PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 20).addBox(-0.5F, -5.0F, 0.0F, 1.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3997F, -4.005F, 0.0F, 0.0F, 0.3491F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}