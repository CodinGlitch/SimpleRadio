package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TrinketsCompat {

    public static void initialize() {
        Services.PLATFORM.forClient(() -> TrinketRendererRegistry.registerRenderer(SimpleRadioItems.TRANSCEIVER, TrinketHandheldRenderer.INSTANCE));
    }

    public static void postInitialize() {
        AccessoryCompat.makeItems(((accessory, ticker) -> {

            TrinketsApi.registerTrinket(accessory, new Trinket() {
                @Override
                public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
                    if (ticker != null) {
                        ticker.accept(new AccessoryCompat.Context(
                                "trinket",
                                entity,
                                0,
                                false,
                                false
                        ), stack);
                    }

                    Trinket.super.tick(stack, slot, entity);
                }
            });

        }));
    }

    public static ItemStack getAccessory(Player player, Predicate<ItemStack> filter) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
        if (component.isEmpty()) return ItemStack.EMPTY;

        List<Tuple<SlotReference, ItemStack>> result = component.get().getEquipped(filter);
        if (result.isEmpty()) return ItemStack.EMPTY;

        return result.stream()
                .findFirst()
                .get().getB();
    }

    public static class TrinketHandheldRenderer implements TrinketRenderer {
        public static TrinketHandheldRenderer INSTANCE = new TrinketHandheldRenderer();

        @Override
        public void render(
                ItemStack stack,
                SlotReference slotReference,
                EntityModel<? extends LivingEntity> contextModel,
                PoseStack poseStack,
                MultiBufferSource buffer,
                int light, LivingEntity livingEntity,
                float limbAngle, float limbDistance,
                float tickDelta, float animationProgress,
                float headYaw, float headPitch
        ) {
            if (!(contextModel instanceof HumanoidModel<?>)) return;

            AccessoryCompat.HandheldRenderer.render(stack, livingEntity, poseStack, buffer, light);
        }
    }
}
