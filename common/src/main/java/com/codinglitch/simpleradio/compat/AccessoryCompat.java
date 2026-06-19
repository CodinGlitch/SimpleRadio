package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import com.codinglitch.simpleradio.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Meant to serve as an abstracted layer between accessory mods of different loaders.
 */
public class AccessoryCompat {

    public record Context(String identifier, LivingEntity entity, int index, boolean cosmetic, boolean visible) {}

    @FunctionalInterface
    public interface AccessoryRegistry {
        void register(Item accessory, BiConsumer<Context, ItemStack> ticker);
    }

    public static void makeItems(AccessoryRegistry registry, BiConsumer<Item, AccessoryRenderer> rendererProvider) {
        BiConsumer<Context, ItemStack> handheldTicker = (context, stack) -> {
            if (!context.identifier().equals("trinket")) return;

            // inventoryTick is not called for Trinkets inventory, so we call it manually to keep routers alive
            if (stack.getItem() instanceof TransceiverItem transceiverItem) {
                transceiverItem.entityTick(stack, context.entity);
            }
        };

        registry.register(SimpleRadioItems.TRANSCEIVER, handheldTicker);
        registry.register(SimpleRadioItems.WALKIE_TALKIE, handheldTicker);
        registry.register(SimpleRadioItems.SPUDDIE_TALKIE, handheldTicker);

        Services.PLATFORM.forClient(() -> {
            rendererProvider.accept(SimpleRadioItems.TRANSCEIVER, new HandheldRenderer());
            rendererProvider.accept(SimpleRadioItems.WALKIE_TALKIE, new HandheldRenderer());
            rendererProvider.accept(SimpleRadioItems.SPUDDIE_TALKIE, new HandheldRenderer());
        });

    }

    public static ItemStack getAccessory(Player player, Predicate<ItemStack> filter) {
        return Services.COMPAT.getAccessory(player, filter);
    }

    public static void setHandheld(Player player, boolean state) {
        // our getAccessory *should* theoretically yield the same item stack on both sides.. right?
        ItemStack stack = AccessoryCompat.getAccessory(player, test -> test.getItem() instanceof TransceiverItem);
        if (stack.isEmpty()) return;

        CommonSimpleRadio.info(state);

        TransceiverItem transceiver = (TransceiverItem) stack.getItem();
        if (state) {
            transceiver.begin(stack, player);
        } else {
            transceiver.end(stack, player);
        }
    }

    public abstract static class AccessoryRenderer {
        public abstract <T extends LivingEntity, M extends EntityModel<T>> void render(
                ItemStack stack,
                LivingEntity livingEntity,
                PoseStack poseStack,
                MultiBufferSource buffer,
                int light
        );
    }

    public static class HandheldRenderer extends AccessoryRenderer {

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(
                ItemStack stack,
                LivingEntity livingEntity,
                PoseStack poseStack,
                MultiBufferSource buffer,
                int light
        ) {
            Minecraft minecraft = Minecraft.getInstance();

            poseStack.pushPose();

            poseStack.translate(-0.125f, 0.1f, -0.15f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.scale(0.4f, 0.4f, 0.4f);

            // Tilt it slightly towards the head if using
            if (stack.hasTag() && stack.getTag().contains("using") && stack.getTag().getBoolean("using")) {
                poseStack.translate(0.0f, 0.2f, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(30));
                poseStack.mulPose(Axis.YP.rotationDegrees(15));
            }

            minecraft.getItemRenderer().renderStatic(
                    stack, ItemDisplayContext.FIXED,
                    light, 0,
                    poseStack, buffer, livingEntity.level(), 0
            );

            poseStack.popPose();
        }
    }
}
