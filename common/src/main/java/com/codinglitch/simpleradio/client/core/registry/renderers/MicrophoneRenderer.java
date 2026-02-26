package com.codinglitch.simpleradio.client.core.registry.renderers;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.client.core.registry.models.MicrophoneModel;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.blocks.MicrophoneBlock;
import com.codinglitch.simpleradio.core.registry.blocks.MicrophoneBlockEntity;
import com.codinglitch.simpleradio.routers.Router;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector3f;

import java.awt.*;

public class MicrophoneRenderer implements BlockEntityRenderer<MicrophoneBlockEntity> {
    private MicrophoneModel model;

    public MicrophoneRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MicrophoneModel(context.bakeLayer(MicrophoneModel.LAYER_LOCATION));
    }

    @Override
    public void render(MicrophoneBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        Block block = state.getBlock();

        if (block instanceof MicrophoneBlock microphoneBlock && blockEntity.id != null) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 1.5f, 0.5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.mulPose(Axis.YP.rotationDegrees(microphoneBlock.getYRotationDegrees(state)));

            float targetTilt = blockEntity.tilt - 1.5f;
            blockEntity.currentTilt = Math.lerp(blockEntity.currentTilt, targetTilt, Math.min(Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() * 0.3f, 1));

            model.plug.visible = !blockEntity.getWires().isEmpty();

            model.body.xRot = blockEntity.currentTilt;

            Router router = ClientRadioManager.getInstance().getRouter(blockEntity.id); // workaround for create
            if (router != null) {
                float rotation = Math.toRadians(SimpleRadioBlocks.MICROPHONE.getYRotationDegrees(state) - 90);
                float tilt = blockEntity.currentTilt - 0.5f;
                Vector3f normal = new Vector3f(Math.cos(rotation), 0, Math.sin(rotation));

                router.setConnectionOffset(new Vec3(
                        normal.x * Math.cos(tilt)*0.25f,
                        Math.sin(tilt)*0.25f,
                        normal.z * Math.cos(tilt)*0.25f
                ));
            }

            VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(blockEntity.isListening() ? MicrophoneModel.ACTIVE_LOCATION : MicrophoneModel.TEXTURE_LOCATION));
            model.renderToBuffer(poseStack, vertexConsumer, light, overlay, Color.white.getRGB());

            poseStack.popPose();
        }
    }
}
