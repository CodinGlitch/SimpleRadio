package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

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

    public static void makeItems(AccessoryRegistry registry) {
        BiConsumer<Context, ItemStack> handheldTicker = (context, stack) -> {
            if (!context.identifier().equals("trinket")) return;

            // inventoryTick is not called for Trinkets inventory, so we call it manually to keep routers alive
            if (stack.getItem() instanceof TransceiverItem transceiverItem) {
                transceiverItem.entityTick(stack, context.entity);
            }
        };

        registry.register(SimpleRadioItems.TRANSCEIVER, handheldTicker);
        registry.register(SimpleRadioItems.WALKIE_TALKIE, handheldTicker);
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
}
