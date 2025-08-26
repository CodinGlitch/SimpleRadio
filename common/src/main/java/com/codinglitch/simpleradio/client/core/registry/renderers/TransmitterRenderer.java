package com.codinglitch.simpleradio.client.core.registry.renderers;

import com.codinglitch.simpleradio.core.registry.blocks.TransmitterBlock;
import com.codinglitch.simpleradio.core.registry.blocks.TransmitterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class TransmitterRenderer implements BlockEntityRenderer<TransmitterBlockEntity> {
    public TransmitterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TransmitterBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        if (!blockEntity.hasLevel()) return;

        BlockState state = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos());
        if (state.isAir() || !(state.getBlock() instanceof TransmitterBlock)) return;

        if (blockEntity.catalyst != null) {
            poseStack.pushPose();

            FrequencingRenderer.renderCatalyst(blockEntity, state, poseStack, bufferSource, light, overlay);
            poseStack.popPose();
        }

        if (blockEntity.frequency != null) {
            poseStack.pushPose();

            // Center our text before rotating it according to the states rotation
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-state.getValue(TransmitterBlock.FACING).toYRot()));

            // We can add our offset now that we are aligned with the block
            poseStack.translate(0f, 0.251f, -0.225f);
            poseStack.scale(-0.01F, -0.01F, 0.01F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));

            FrequencingRenderer.renderScreen(blockEntity, state, poseStack, bufferSource, light, overlay);

            poseStack.popPose();
        }
    }
}
