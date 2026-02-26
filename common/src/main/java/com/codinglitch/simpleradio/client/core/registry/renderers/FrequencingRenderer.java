package com.codinglitch.simpleradio.client.core.registry.renderers;

import com.codinglitch.simpleradio.core.registry.CatalystRegistry;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Catalyst;
import com.codinglitch.simpleradio.central.Frequencing;
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
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Math;
import org.joml.Vector3f;

import java.util.List;

public class FrequencingRenderer {
    public static final int FRAME_RATE = 12;

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
            List<Catalyst> catalysts = CatalystRegistry.getCatalysts();
            float catalystSize = 8f;
            float offset = catalystSize/2f - (catalysts.size() / 2f) * catalystSize;
            for (int i = 0; i < catalysts.size(); i++) {
                Catalyst catalyst = catalysts.get(i);

                poseStack.pushPose();
                poseStack.translate(offset + i*catalystSize, 10f, 0f);
                poseStack.scale(catalystSize, catalystSize, 0.01f);
                //poseStack.mulPose(Axis.YP.rotationDegrees(time*60));

                minecraft.getItemRenderer().renderStatic(
                        new ItemStack(catalyst.associate), ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT, overlay,
                        poseStack, bufferSource, blockEntity.getLevel(), 0
                );
                poseStack.popPose();
            }

            String text = I18n.get("screen.simpleradio.frequencing.catalyst");
            poseStack.scale(1f, 1f, 1f);
            poseStack.translate(0.5f, -5f, 0);

            float width = -font.width(text) /**// 2f;
            font.drawInBatch(text, width, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);

            return;
        }

        float time = (level.getGameTime() + Minecraft.getInstance().getTimer().getRealtimeDeltaTicks())/20; // in SECONDS bro
        time = Math.floor(time*FRAME_RATE)/FRAME_RATE;

        //--- Catalyst Display ---\\
        poseStack.pushPose();
        poseStack.translate(24f, -5f, 0f);
        poseStack.scale(10f, 10f, 0.01f);
        poseStack.mulPose(Axis.YP.rotationDegrees(time*60));

        minecraft.getItemRenderer().renderStatic(
                new ItemStack(blockEntity.catalyst.associate), ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT,
                OverlayTexture.pack((int) Math.floor((Math.sin(time * 5f) + 1) * 5), 15), poseStack, bufferSource, blockEntity.getLevel(), 0
        );
        poseStack.popPose();


        poseStack.pushPose();
        poseStack.translate(-28f, -4f, 0f);
        poseStack.scale(0.4f, 0.4f, 0.4f);

        String text = Component.translatable(
                "screen.simpleradio.frequencing.efficiency",
                Math.round(blockEntity.catalyst.efficiency*100)
        ).getString();
        font.drawInBatch(text, 0, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);
        poseStack.popPose();

        //--- Antenna Power Display ---\\

        int antennaPower = frequencing.getAntennaPower();
        String antenna = String.valueOf(frequencing.getAntennaPower());

        float progress = Mth.clamp(antennaPower / 100f, 0, 1);

        poseStack.pushPose();
        poseStack.translate(-28f, 8f, 0f);
        poseStack.scale(0.4f, 0.4f, 0.4f);

        font.drawInBatch(I18n.get("screen.simpleradio.frequencing.antenna_strength"), 0, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-23 + progress*40, 14f, 0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        //antenna = String.valueOf((int) (progress * 100));
        float width = (float) (-font.width(antenna) / 2);
        font.drawInBatch(antenna, width, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-8, 16f, 0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        Vector3f one = poseStack.last().pose().transformPosition(new Vector3f(-40, -5, 0));
        Vector3f two = poseStack.last().pose().transformPosition(new Vector3f(-40, 5, 0));
        Vector3f three = poseStack.last().pose().transformPosition(new Vector3f(-40 + (progress*80), 5, 0));
        Vector3f four = poseStack.last().pose().transformPosition(new Vector3f(-40 + (progress*80), -5, 0));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(CommonSimpleRadio.id("textures/gui/bars.png")));
        consumer.addVertex(one.x, one.y, one.z).setColor(1f, 1f, 1f, 1f).setUv(0f, 0f)
                .setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        consumer.addVertex(two.x, two.y, two.z).setColor(1f, 1f, 1f, 1f).setUv(0f, 1f)
                .setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        consumer.addVertex(three.x, three.y, three.z).setColor(1f, 1f, 1f, 1f).setUv(progress, 1f)
                .setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);
        consumer.addVertex(four.x, four.y, four.z).setColor(1f, 1f, 1f, 1f).setUv(progress, 0f)
                .setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0, 0, 1);

        poseStack.popPose();

        //--- Frequency Display ---\\
        String frequency = frequencing.getFrequency(blockEntity).toString();

        poseStack.pushPose();
        poseStack.translate(-28f, -10f, 0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        font.drawInBatch(frequency, 0, 0, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 255);
        poseStack.popPose();
    }
}
