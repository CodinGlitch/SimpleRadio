package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.function.Function;

public class CuriosCompat {

    public static void postInitialize() {
        AccessoryCompat.makeItems(((accessory, ticker) -> {

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

        }));
    }
}
