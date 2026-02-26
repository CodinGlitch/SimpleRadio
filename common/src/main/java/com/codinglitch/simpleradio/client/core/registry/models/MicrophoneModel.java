package com.codinglitch.simpleradio.client.core.registry.models;// Made with Blockbench 4.10.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MicrophoneModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(CommonSimpleRadio.id("microphonemodel"), "main");
	public static final ResourceLocation TEXTURE_LOCATION = CommonSimpleRadio.id("textures/block/microphone.png");
	public static final ResourceLocation ACTIVE_LOCATION = CommonSimpleRadio.id("textures/block/microphone_active.png");
	private final ModelPart bone;
	public final ModelPart body;
	public final ModelPart plug;

	public MicrophoneModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.bone = root.getChild("bone");
		this.body = this.bone.getChild("body");
		this.plug = this.body.getChild("plug");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(18, 3).addBox(-9.0F, -2.0F, 7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-12.0F, -3.0F, 7.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16).addBox(-11.0F, -1.0F, 5.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(20, 12).addBox(-5.0F, -10.0F, 6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(20, 12).mirror().addBox(-13.0F, -10.0F, 6.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(0, 0).addBox(-12.0F, -6.0F, 7.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-5.0F, -6.0F, 7.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

		PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 1.0F, -4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -8.0F, 8.0F));

		PartDefinition plug = body.addOrReplaceChild("plug", CubeListBuilder.create().texOffs(18, 2).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 4.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}