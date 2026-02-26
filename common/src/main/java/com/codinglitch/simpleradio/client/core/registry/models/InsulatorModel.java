package com.codinglitch.simpleradio.client.core.registry.models;

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

public class InsulatorModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(CommonSimpleRadio.id("insulatormodel"), "main");
	public static final ResourceLocation TEXTURE_LOCATION = CommonSimpleRadio.id("textures/block/insulator.png");
	private final ModelPart bone;
	public final ModelPart spool;
	public final ModelPart wire;

	public InsulatorModel(ModelPart root) {
		super(RenderType::entitySolid);
		this.bone = root.getChild("bone");
		this.spool = this.bone.getChild("spool");
		this.wire = this.spool.getChild("wire");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(12, 12).addBox(-6F, -5.0F, 7.01F, 1.0F, 7.0F, 1.98F, new CubeDeformation(0.0F))
				.texOffs(12, 12).mirror().addBox(-11F, -5.0F, 7.01F, 1.0F, 7.0F, 1.98F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(8.0F, 24.0F, -8.0F));

		PartDefinition spool = bone.addOrReplaceChild("spool", CubeListBuilder.create().texOffs(14, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(1.0F, -3.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).mirror().addBox(-2.0F, -3.0F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-8.0F, -4.0F, 8.0F));

		PartDefinition wire = spool.addOrReplaceChild("wire", CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}