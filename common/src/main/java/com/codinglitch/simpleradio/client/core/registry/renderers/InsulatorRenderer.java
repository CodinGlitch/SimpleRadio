package com.codinglitch.simpleradio.client.core.registry.renderers;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.client.core.registry.models.InsulatorModel;
import com.codinglitch.simpleradio.core.registry.blocks.InsulatorBlock;
import com.codinglitch.simpleradio.core.registry.blocks.InsulatorBlockEntity;
import com.codinglitch.simpleradio.routers.Router;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;

import java.awt.*;

public class InsulatorRenderer implements BlockEntityRenderer<InsulatorBlockEntity> {
    private InsulatorModel model;

    public InsulatorRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new InsulatorModel(context.bakeLayer(InsulatorModel.LAYER_LOCATION));
    }

    @Override
    public void render(InsulatorBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        Block block = state.getBlock();

        if (block instanceof InsulatorBlock insulatorBlock && blockEntity.id != null) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);

            Direction facing = state.getValue(InsulatorBlock.FACING);
            poseStack.mulPose(facing.getRotation());
            poseStack.translate(0f, 1f, 0f);

            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.mulPose(Axis.YP.rotationDegrees(state.getValue(InsulatorBlock.ROTATED) ? 90 : 0));

            float rotation = 0.5f;
            if (blockEntity.connector != null) {
                Vec3 pos = blockEntity.getBlockPos().getCenter();
                rotation = (float) (3f + blockEntity.connector.distanceToSqr(pos.x, pos.y, pos.z)*0.5f);
            }

            blockEntity.rotation = Math.lerp(blockEntity.rotation, rotation, Math.min(Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() * 0.2f, 1));

            model.wire.visible = !blockEntity.getWires().isEmpty() || blockEntity.connector != null;
            model.spool.xRot = blockEntity.rotation;

            Router router = ClientRadioManager.getInstance().getRouter(blockEntity.id);
            if (router != null) {
                /*float rotation = Math.toRadians(SimpleRadioBlocks.MICROPHONE.getYRotationDegrees(state) - 90);
                float tilt = blockEntity.currentTilt - 0.5f;
                Vector3f normal = new Vector3f(Math.cos(rotation), 0, Math.sin(rotation));

                router.connectionOffset = new Vec3(
                        normal.x * Math.cos(tilt)*0.25f,
                        Math.sin(tilt)*0.25f,
                        normal.z * Math.cos(tilt)*0.25f
                );*/
            }

            VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(InsulatorModel.TEXTURE_LOCATION));
            model.renderToBuffer(poseStack, vertexConsumer, light, overlay, Color.white.getRGB());

            poseStack.popPose();
        }
    }
}
