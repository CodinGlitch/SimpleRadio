package com.codinglitch.simpleradio.client.renderers;

import com.codinglitch.simpleradio.client.models.RadioModel;
import com.codinglitch.simpleradio.core.registry.blocks.RadioBlock;
import com.codinglitch.simpleradio.core.registry.blocks.RadioBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RadioRenderer implements BlockEntityRenderer<RadioBlockEntity> {
    private RadioModel model;

    public RadioRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new RadioModel(context.bakeLayer(RadioModel.LAYER_LOCATION));
    }

    @Override
    public void render(RadioBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        Block block = state.getBlock();

        if (block instanceof RadioBlock radioBlock) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 1.5f, 0.5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.mulPose(Axis.YP.rotationDegrees(radioBlock.getYRotationDegrees(state)));

            VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(RadioModel.TEXTURE_LOCATION));
            model.renderToBuffer(poseStack, vertexConsumer, light, overlay, 1, 1, 1, 1);

            poseStack.popPose();
        }
    }
}
