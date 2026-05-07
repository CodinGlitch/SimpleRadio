package com.codinglitch.simpleradio.client.core.registry.renderers;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.client.ClientCompat;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioRouter;
import com.codinglitch.simpleradio.routers.Router;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;

import java.util.Optional;
import java.util.UUID;

import static com.codinglitch.simpleradio.core.SimpleRadioComponents.WIRE_TARGET;

public class WireRenderer extends EntityRenderer<Wire> {
    private static final Vec3 UP = new Vec3(0, 1, 0);
    private static final Vec3 RIGHT = new Vec3(1, 0, 0);

    private static final float CABLE_SIZE = 0.05f;
    private static final float SEGMENTS = 24;

    public WireRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static void renderWire(Level level, MultiBufferSource source, PoseStack poseStack, Vec3 from, Vec3 to, @Nullable Wire wire, float partialTick) {
        VertexConsumer consumer = source.getBuffer(RenderType.entitySolid(CommonSimpleRadio.id("textures/entity/wire.png")));

        Matrix4f matrix = poseStack.last().pose();

        float distance = (float) (to.subtract(from)).length();

        Vec3 middle = from.lerp(to, 0.5).subtract(0, SimpleRadioLibrary.CLIENT_CONFIG.wire.baseSag + (distance*SimpleRadioLibrary.CLIENT_CONFIG.wire.distanceSag), 0);

        Vec3 lastTopLeft = null;
        Vec3 lastBottomLeft = null;
        Vec3 lastTopRight = null;
        Vec3 lastBottomRight = null;

        int fromSkyLight = level.getBrightness(LightLayer.SKY, BlockPos.containing(from));
        int toSkyLight = level.getBrightness(LightLayer.SKY, BlockPos.containing(to));

        int fromBlockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(from));
        int toBlockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(to));

        float tile = 0f;//distance*(1f/24f)*CABLE_SIZE;
        float vOffset = ((distance/CABLE_SIZE)/SEGMENTS) * 0.065f;

        for (int i = 0; i <= SEGMENTS; i++) {
            float progress = i/SEGMENTS;

            float effector = 0;
            if (wire != null && SimpleRadioLibrary.CLIENT_CONFIG.wire.effect) {
                double effectDuration = distance * SimpleRadioLibrary.CLIENT_CONFIG.wire.effectTime;
                for (Wire.Effect effect : wire.effectList) {
                    float effectProgress = (float) ((effect.progress + (effect.direction * partialTick)) / effectDuration);

                    float effectDistance = Math.abs(effectProgress - progress);
                    if (effectDistance <= 0.1f) {
                        effector += (0.1f - effectDistance) * 10f;
                    }
                }
            }

            Vec3 fromMiddle = from.lerp(middle, progress);
            Vec3 middleTo = middle.lerp(to, progress);

            Vec3 segmentPosition = fromMiddle.lerp(middleTo, progress);

            Vec3 direction = fromMiddle.lerp(middleTo, progress + 0.05f).subtract(segmentPosition).normalize();

            Vec3 side = direction.cross((direction.y == -1 || direction.y == 1) ? RIGHT : UP).normalize().scale(CABLE_SIZE/2);
            Vec3 up = side.cross(direction).normalize().scale(CABLE_SIZE/2);

            Vec3 otherSide = side.reverse();
            Vec3 down = up.reverse();

            Vec3 topLeft = segmentPosition.add(otherSide).add(up);
            Vec3 bottomLeft = segmentPosition.add(otherSide).add(down);
            Vec3 topRight = segmentPosition.add(side).add(up);
            Vec3 bottomRight = segmentPosition.add(side).add(down);

            if (lastTopLeft != null) {
                int skyLight = (int) Mth.lerp(progress, (float)fromSkyLight, (float)toSkyLight);
                int blockLight = (int) Mth.lerp(
                        Math.clamp(0, 1, effector),
                        Mth.lerp(progress, (float)fromBlockLight, (float)toBlockLight),
                        15f
                );

                float newTile = vOffset + ((vOffset*SEGMENTS) * progress);

                int overlay = Math.clamp(0, 10, Math.round(effector*5));
                int packedLight = LightTexture.pack(blockLight, skyLight);

                buildQuad(consumer, matrix, overlay, packedLight, up.normalize(), 0.0625f, vOffset, newTile, lastTopRight, topRight, topLeft, lastTopLeft);
                buildQuad(consumer, matrix, overlay, packedLight, side.normalize(), 2f, vOffset, newTile, lastBottomRight, bottomRight, topRight, lastTopRight);
                buildQuad(consumer, matrix, overlay, packedLight, down.normalize(), 1.1875f, vOffset, newTile, lastBottomLeft, bottomLeft, bottomRight, lastBottomRight);
                buildQuad(consumer, matrix, overlay, packedLight, otherSide.normalize(), 3.25f, vOffset, newTile, lastTopLeft, topLeft, bottomLeft, lastBottomLeft);
            }

            lastTopLeft = topLeft;
            lastBottomLeft = bottomLeft;
            lastTopRight = topRight;
            lastBottomRight = bottomRight;
        }
    }

    @Override
    public void render(Wire wire, float f, float partialTick, PoseStack poseStack, MultiBufferSource source, int i) {
        Optional<UUID> fromRef = wire.getFrom();
        Optional<UUID> toRef = wire.getTo();

        if (fromRef.isPresent()) {
            RadioRouter from = (RadioRouter) ClientRadioManager.getInstance().getRouter(fromRef.get());
            if (from == null) return;

            //wire.setPos(new Vec3(from.getLocation().position()));

            if (toRef.isPresent()) {
                RadioRouter to = (RadioRouter) ClientRadioManager.getInstance().getRouter(toRef.get());
                if (to == null) return;

                Vec3 fromPosition = from.getConnectionPosition();
                Vec3 toPosition = to.getConnectionPosition();

                Vec3 offset = wire.getPosition(partialTick);

                poseStack.pushPose();
                poseStack.translate(-offset.x, -offset.y, -offset.z);

                renderWire(wire.level(), source, poseStack, fromPosition, toPosition, wire, partialTick);

                poseStack.popPose();
            }
        }

        super.render(wire, f, partialTick, poseStack, source, i);
    }

    public static void renderPlayer(AbstractClientPlayer player, MultiBufferSource source, PoseStack poseStack, float partialTick, @Nullable Camera camera) {
        ItemStack wire = RadioManager.getInstance().isEntityHolding(player, stack -> stack.is(SimpleRadioItems.COPPER_WIRE));
        if (wire != null) {
            if (wire.has(WIRE_TARGET)) {
                Router router = ClientRadioManager.getInstance().getRouter(wire.get(WIRE_TARGET));
                if (router == null) return;

                ClientLevel level = player.clientLevel;

                Vec3 holdPosition = player.getRopeHoldPosition(partialTick);
                Vec3 connectionPosition = router.getConnectionPosition();

                Vec3 offset = player.getPosition(partialTick);

                poseStack.pushPose();
                if (camera != null) {
                    Vec3 cameraPos = camera.getPosition();
                    poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

                    renderWire(level, source, poseStack, holdPosition, connectionPosition, null, partialTick);
                } else {
                    poseStack.translate(-offset.x, -offset.y, -offset.z);

                    renderWire(level, source, poseStack, holdPosition, connectionPosition, null, partialTick);
                }
                poseStack.popPose();
            }
        }
    }

    public static void buildQuad(VertexConsumer consumer, Matrix4f matrix, int overlay, int packedLight, Vec3 normal, float index, float offset, float tile, Vec3 one, Vec3 two, Vec3 three, Vec3 four) {
        consumer.addVertex(matrix, (float) one.x, (float) one.y, (float) one.z).setColor(1f, 1f, 1f, 1f).setUv(index, tile)
                .setOverlay(OverlayTexture.pack(overlay, 15)).setLight(packedLight).setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        consumer.addVertex(matrix, (float) two.x, (float) two.y, (float) two.z).setColor(1f, 1f, 1f, 1f).setUv(index, offset + tile)
                .setOverlay(OverlayTexture.pack(overlay, 15)).setLight(packedLight).setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        consumer.addVertex(matrix, (float) three.x, (float) three.y, (float) three.z).setColor(1f, 1f, 1f, 1f).setUv(index, offset + tile)
                .setOverlay(OverlayTexture.pack(overlay, 15)).setLight(packedLight).setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        consumer.addVertex(matrix, (float) four.x, (float) four.y, (float) four.z).setColor(1f, 1f, 1f, 1f).setUv(index, tile)
                .setOverlay(OverlayTexture.pack(overlay, 15)).setLight(packedLight).setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }

    @Override
    public ResourceLocation getTextureLocation(Wire wireEntity) {
        return null;
    }
}
