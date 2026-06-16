package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;

/**
 * Meant to serve as an abstracted layer between accessory mods of different loaders.
 */
public class AccessoryCompat {

    public record Context(String identifier, LivingEntity entity, int index, boolean cosmetic, boolean visible) {}

    @FunctionalInterface
    public interface AccessoryRegistry {
        void register(Item accessory, BiConsumer<Context, ItemStack> ticker);
    }

    public static void makeItems(AccessoryRegistry registry) {
        BiConsumer<Context, ItemStack> handheldTicker = (context, stack) -> {
            if (context.identifier().equals("trinket")) return;

            // inventoryTick is not called for Trinkets inventory, so we call it manually to keep routers alive
            if (stack.getItem() instanceof TransceiverItem transceiverItem) {
                transceiverItem.entityTick(stack, context.entity);
            }
        };

        registry.register(SimpleRadioItems.TRANSCEIVER, null);
        registry.register(SimpleRadioItems.WALKIE_TALKIE, null);
    }
}
