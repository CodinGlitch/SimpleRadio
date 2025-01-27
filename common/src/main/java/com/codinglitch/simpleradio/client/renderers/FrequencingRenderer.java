package com.codinglitch.simpleradio.client.renderers;

import com.codinglitch.simpleradio.core.central.Frequencing;
import com.codinglitch.simpleradio.core.registry.blocks.CatalyzingBlockEntity;
import com.codinglitch.simpleradio.core.registry.blocks.ReceiverBlock;
import com.codinglitch.simpleradio.core.registry.blocks.ReceiverBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class FrequencingRenderer {
    public static void renderCatalyst(CatalyzingBlockEntity blockEntity, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();

        poseStack.translate(0.5f, 0.1f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.getValue(ReceiverBlock.FACING).toYRot()));

        Item item = blockEntity.catalyst.associate;

        if (item instanceof BlockItem blockItem) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(-0.5f, 0f, -0.75f);

            minecraft.getBlockRenderer().renderSingleBlock(
                    blockItem.getBlock().defaultBlockState(),
                    poseStack, bufferSource,
                    light, overlay
            );
        } else {
            minecraft.getItemRenderer().renderStatic(
                    new ItemStack(item), ItemDisplayContext.FIXED,
                    light, overlay,
                    poseStack, bufferSource, blockEntity.getLevel(), 0
            );
        }
    }

    public static void renderScreen(CatalyzingBlockEntity blockEntity, BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        if (!(blockEntity instanceof Frequencing frequencing)) return;

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        String text = "";
        if (blockEntity.catalyst != null) {
            text = frequencing.getFrequency(blockEntity).toString();
        } else {
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(0, 20f, 0f);

            float width = (float) (-font.width("CATALYST") / 2);
            font.drawInBatch("CATALYST", width, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);

            //minecraft.getItemRenderer().renderStatic(new ItemStack(Items.GILDED_BLACKSTONE), ItemDisplayContext.GUI, light, overlay, poseStack, bufferSource, blockEntity.getLevel(), 0);

            text = "✖";
            poseStack.scale(4f, 4f, 4f);
            poseStack.translate(0, -9f, 0);
        }

        // Center-aligned text
        float width = (float) (-font.width(text) / 2);
        font.drawInBatch(text, width, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);
    }
}
