package com.codinglitch.simpleradio.client.core.registry.renderers;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.client.core.registry.models.MicrophoneModel;
import com.codinglitch.simpleradio.client.core.registry.models.SocketModel;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.blocks.MicrophoneBlock;
import com.codinglitch.simpleradio.core.registry.blocks.MicrophoneBlockEntity;
import com.codinglitch.simpleradio.core.registry.blocks.SocketBlock;
import com.codinglitch.simpleradio.core.registry.blocks.SocketBlockEntity;
import com.codinglitch.simpleradio.radio.RadioRouter;
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

public class SocketRenderer implements BlockEntityRenderer<SocketBlockEntity> {
    private SocketModel model;

    public SocketRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new SocketModel(context.bakeLayer(SocketModel.LAYER_LOCATION));
    }

    @Override
    public void render(SocketBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        Block block = state.getBlock();

        if (block instanceof SocketBlock socketBlock) {
            poseStack.pushPose();

            model.wire.visible = !blockEntity.getWires().isEmpty();

            RadioRouter router = ClientRadioManager.getRouter(blockEntity.getReference());
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

            VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(SocketModel.TEXTURE_LOCATION));
            model.renderToBuffer(poseStack, vertexConsumer, light, overlay, 1, 1, 1, 1);

            poseStack.popPose();
        }
    }
}
