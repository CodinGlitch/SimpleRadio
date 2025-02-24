package com.codinglitch.simpleradio.client.renderers;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.Frequencing;
import com.codinglitch.simpleradio.core.registry.blocks.CatalyzingBlockEntity;
import com.codinglitch.simpleradio.core.registry.blocks.ReceiverBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

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

        Level level = blockEntity.getLevel();
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        if (level == null) return;

        if (blockEntity.catalyst == null) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(0, 20f, 0f);

            String text = I18n.get("screen.simpleradio.frequencing.catalyst");
            float width = (float) (-font.width(text) / 2);
            font.drawInBatch(text, width, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);

            //minecraft.getItemRenderer().renderStatic(new ItemStack(Items.GILDED_BLACKSTONE), ItemDisplayContext.GUI, light, overlay, poseStack, bufferSource, blockEntity.getLevel(), 0);
            text = "✖";
            poseStack.scale(4f, 4f, 4f);
            poseStack.translate(0, -9f, 0);

            width = (float) (-font.width(text) / 2);
            font.drawInBatch(text, width, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);

            return;
        }

        //--- Catalyst Display ---\\
        poseStack.pushPose();
        poseStack.translate(24f, -5f, 0f);
        poseStack.scale(10f, 10f, 0.01f);

        float time = level.getGameTime() + Minecraft.getInstance().getFrameTime();

        minecraft.getItemRenderer().renderStatic(
                new ItemStack(blockEntity.catalyst.associate), ItemDisplayContext.GUI, light,
                OverlayTexture.pack((int) Math.floor((Math.sin(time * 0.2f) + 1) * 5), 15), poseStack, bufferSource, blockEntity.getLevel(), 0
        );
        poseStack.popPose();


        //--- Antenna Power Display ---\\
        int antennaPower = frequencing.getAntennaPower();
        String antenna = String.valueOf(frequencing.getAntennaPower());

        float progress = Mth.clamp(antennaPower / 100f, 0, 1);

        poseStack.pushPose();
        poseStack.translate(-24 + progress*40, 12f, 0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        //antenna = String.valueOf((int) (progress * 100));
        float width = (float) (-font.width(antenna) / 2);
        font.drawInBatch(antenna, width, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-9, 14f, 0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        Vector3f one = poseStack.last().pose().transformPosition(new Vector3f(-40, -5, 0));
        Vector3f two = poseStack.last().pose().transformPosition(new Vector3f(-40, 5, 0));
        Vector3f three = poseStack.last().pose().transformPosition(new Vector3f(-40 + (progress*80), 5, 0));
        Vector3f four = poseStack.last().pose().transformPosition(new Vector3f(-40 + (progress*80), -5, 0));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(CommonSimpleRadio.id("textures/gui/bars.png")));
        consumer.vertex(one.x, one.y, one.z).color(1f, 1f, 1f, 1f).uv(0f, 0f)
                .overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        consumer.vertex(two.x, two.y, two.z).color(1f, 1f, 1f, 1f).uv(0f, 1f)
                .overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        consumer.vertex(three.x, three.y, three.z).color(1f, 1f, 1f, 1f).uv(progress, 1f)
                .overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();
        consumer.vertex(four.x, four.y, four.z).color(1f, 1f, 1f, 1f).uv(progress, 0f)
                .overlayCoords(overlay).uv2(LightTexture.FULL_BRIGHT).normal(0, 0, 1).endVertex();

        poseStack.popPose();

        //--- Frequency Display ---\\
        String frequency = frequencing.getFrequency(blockEntity).toString();

        poseStack.pushPose();
        poseStack.translate(0f, -8f, 0f);
        poseStack.scale(0.65f, 0.65f, 0.65f);
        width = (float) -font.width(frequency);

        font.drawInBatch(frequency, width, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);
        poseStack.popPose();
    }
}
