package com.codinglitch.simpleradio.client.renderers;

import com.codinglitch.simpleradio.core.central.AuditoryBlockEntity;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Function;

public class WireRenderer {
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final float CABLE_SIZE = 0.075f;

    public static void renderPlayer(AbstractClientPlayer player, MultiBufferSource source, PoseStack poseStack, float partialTick) {
        ItemStack wire = RadioManager.isEntityHolding(player, stack -> stack.is(SimpleRadioItems.COPPER_WIRE));
        if (wire != null) {
            CompoundTag tag = wire.getOrCreateTag();
            if (tag.contains("connectTo")) {
                BlockPos pos = BlockPos.of(tag.getLong("connectTo"));

                ClientLevel level = player.clientLevel;
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AuditoryBlockEntity auditoryBlockEntity) {
                    VertexConsumer consumer = source.getBuffer(RenderType.entityTranslucentCull(new ResourceLocation("textures/block/copper_block.png")));

                    RenderSystem.enableBlend();

                    poseStack.pushPose();

                    Vec3 holdPosition = player.getRopeHoldPosition(partialTick);
                    Vec3 connectionPosition = auditoryBlockEntity.getConnectionPosition();

                    Vec3 offset = connectionPosition.subtract(player.getPosition(partialTick));
                    poseStack.translate(offset.x, offset.y, offset.z);
                    Matrix4f matrix = poseStack.last().pose();

                    Vec3 to = holdPosition.subtract(connectionPosition);
                    float distance = (float) to.length();

                    Vec3 middle = to.scale(0.5).subtract(0, 0.75f + (distance*0.075f), 0);

                    Vec3 lastTopLeft = null;
                    Vec3 lastBottomLeft = null;
                    Vec3 lastTopRight = null;
                    Vec3 lastBottomRight = null;

                    int fromSkyLight = 15;
                    int toSkyLight = level.getBrightness(LightLayer.SKY, BlockPos.containing(holdPosition));

                    int fromBlockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(connectionPosition));
                    int toBlockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(holdPosition));

                    float tile = 0f;//distance*(1f/24f)*CABLE_SIZE;
                    float vOffset = ((distance/CABLE_SIZE)/24f) * 0.4f;

                    for (int i = 0; i <= 24; i++) {
                        float progress = i/24f;

                        Vec3 fromMiddle = middle.scale(progress);
                        Vec3 middleTo = middle.lerp(to, progress);

                        Vec3 segmentPosition = fromMiddle.lerp(middleTo, progress);

                        Vec3 direction = fromMiddle.lerp(middleTo, progress + 0.05f).subtract(segmentPosition).normalize();

                        Vec3 side = direction.cross(UP).normalize().scale(CABLE_SIZE/2);
                        Vec3 up = side.cross(direction).normalize().scale(CABLE_SIZE/2);

                        Vec3 topLeft = segmentPosition.add(side.reverse()).add(up);
                        Vec3 bottomLeft = segmentPosition.add(side.reverse()).add(up.reverse());
                        Vec3 topRight = segmentPosition.add(side).add(up);
                        Vec3 bottomRight = segmentPosition.add(side).add(up.reverse());

                        if (lastTopLeft != null) {
                            int skyLight = (int) Mth.lerp(progress, (float)fromSkyLight, (float)toSkyLight);
                            int blockLight = (int) Mth.lerp(progress, (float)fromBlockLight, (float)toBlockLight);

                            int packedLight = LightTexture.pack(blockLight, skyLight);

                            float newTile = vOffset + ((vOffset*24) * progress);

                            buildQuad(consumer, matrix, packedLight, up, vOffset, newTile, lastTopRight, topRight, topLeft, lastTopLeft);
                            buildQuad(consumer, matrix, packedLight, side, vOffset, newTile, lastBottomRight, bottomRight, topRight, lastTopRight);
                            buildQuad(consumer, matrix, packedLight, up.reverse(), vOffset, newTile, lastBottomLeft, bottomLeft, bottomRight, lastBottomRight);
                            buildQuad(consumer, matrix, packedLight, side.reverse(), vOffset, newTile, lastTopLeft, topLeft, bottomLeft, lastBottomLeft);
                        }

                        lastTopLeft = topLeft;
                        lastBottomLeft = bottomLeft;
                        lastTopRight = topRight;
                        lastBottomRight = bottomRight;
                    }

                    poseStack.popPose();
                }
            }
        }
    }

    public static void buildQuad(VertexConsumer consumer, Matrix4f matrix, int packedLight, Vec3 normal, float offset, float tile, Vec3 one, Vec3 two, Vec3 three, Vec3 four) {
        consumer.vertex(matrix, (float) one.x, (float) one.y, (float) one.z).color(1f, 1f, 1f, 1f).uv(0.6f, tile)
                .overlayCoords(OverlayTexture.NO_WHITE_U).uv2(packedLight).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        consumer.vertex(matrix, (float) two.x, (float) two.y, (float) two.z).color(1f, 1f, 1f, 1f).uv(0.6f, offset + tile)
                .overlayCoords(OverlayTexture.NO_WHITE_U).uv2(packedLight).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        consumer.vertex(matrix, (float) three.x, (float) three.y, (float) three.z).color(1f, 1f, 1f, 1f).uv(0.4f, offset + tile)
                .overlayCoords(OverlayTexture.NO_WHITE_U).uv2(packedLight).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        consumer.vertex(matrix, (float) four.x, (float) four.y, (float) four.z).color(1f, 1f, 1f, 1f).uv(0.4f, tile)
                .overlayCoords(OverlayTexture.NO_WHITE_U).uv2(packedLight).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }
}
