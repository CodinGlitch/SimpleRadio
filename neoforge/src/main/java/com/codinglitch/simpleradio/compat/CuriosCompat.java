package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;
import java.util.function.Predicate;

public class CuriosCompat {

    public static void postInitialize() {
        AccessoryCompat.makeItems((accessory, ticker) -> {

            // we reconstruct the entire item and context to keep it platform-agnostic
            CuriosApi.registerCurio(accessory, new ICurioItem() {
                @Override
                public void curioTick(SlotContext context, ItemStack stack) {
                    if (ticker != null) {
                        ticker.accept(new AccessoryCompat.Context(
                                context.identifier(),
                                context.entity(),
                                context.index(),
                                context.cosmetic(),
                                context.visible()
                        ), stack);
                    }

                    ICurioItem.super.curioTick(context, stack);
                }
            });

        }, (item, renderer) -> {
            CuriosRendererRegistry.register(item, () -> new GenericCurioRenderer(renderer));
        });
    }

    public static ItemStack getAccessory(Player player, Predicate<ItemStack> filter) {
        Optional<ICuriosItemHandler> itemHandler = CuriosApi.getCuriosInventory(player);
        if (itemHandler.isEmpty()) return ItemStack.EMPTY;

        Optional<SlotResult> result = itemHandler.get().findFirstCurio(filter);
        if (result.isEmpty()) return ItemStack.EMPTY;

        return result.get().stack();
    }

    public static void initialize() {
    }

    public static class GenericCurioRenderer implements ICurioRenderer {
        private final AccessoryCompat.AccessoryRenderer internalRenderer;

        public GenericCurioRenderer(AccessoryCompat.AccessoryRenderer internalRenderer) {
            this.internalRenderer = internalRenderer;
        }

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffer,
            int light,
            float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch
        ) {
            LivingEntity livingEntity = slotContext.entity();
            M contextModel = renderLayerParent.getModel();

            if (!(contextModel instanceof HumanoidModel<?>)) return;

            internalRenderer.render(stack, livingEntity, poseStack, buffer, light);
        }
    }
}
