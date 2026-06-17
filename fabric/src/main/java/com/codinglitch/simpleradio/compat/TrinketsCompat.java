package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TrinketsCompat {

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
}
