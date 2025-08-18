package com.codinglitch.simpleradio.client.core.registry.renderers;

import com.codinglitch.simpleradio.core.registry.blocks.FrequencerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

import java.awt.*;

public class FrequencerRenderer implements BlockEntityRenderer<FrequencerBlockEntity> {

    public FrequencerRenderer(BlockEntityRendererProvider.Context context) {
    }

    private void draw(String text, float offset, int color, PoseStack poseStack, MultiBufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();

        poseStack.pushPose();

        poseStack.translate(0.5f, 1.0f + offset, 0.5f);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = poseStack.last().pose();

        Font font = minecraft.font;
        float f2 = (float)(-font.width(text) / 2);

        font.drawInBatch(text, f2, 0, color, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, 255);

        poseStack.popPose();
    }

    @Override
    public void render(FrequencerBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        if (!blockEntity.hasLevel()) return;

        if (blockEntity.frequency == null) {
            if (blockEntity.frequencies.isEmpty()) {
                if (blockEntity.listeners.isEmpty()) {
                    draw("NO FREQUENCIES", 0, Color.red.getRGB(), poseStack, bufferSource);
                } else {
                    draw("Listeners", 0, Color.cyan.getRGB(), poseStack, bufferSource);
                    for (int i = 0; i < blockEntity.listeners.size(); i++) {
                        draw(blockEntity.listeners.get(i), (i+1) * 0.25f, -1, poseStack, bufferSource);
                    }
                }
            } else {
                draw("Frequencies", 0, Color.cyan.getRGB(), poseStack, bufferSource);
                for (int i = 0; i < blockEntity.frequencies.size(); i++) {
                    draw(blockEntity.frequencies.get(i), (i+1) * 0.25f, -1, poseStack, bufferSource);
                }
            }

            return;
        }

        int color = blockEntity.frequencings.size() == 0 ? Color.red.getRGB() : Color.cyan.getRGB();
        draw(blockEntity.frequency.getFrequency() + blockEntity.frequency.getModulation().shorthand, 0, color, poseStack, bufferSource);
        for (int i = 0; i < blockEntity.frequencings.size(); i++) {
            String listener = blockEntity.frequencings.get(i);

            draw(listener, (i+1) * 0.25f, -1, poseStack, bufferSource);
        }
    }
}
