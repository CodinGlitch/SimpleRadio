package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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
}
