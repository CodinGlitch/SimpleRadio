package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.valkyrienskies.core.impl.shadow.It;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

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

    public static ItemStack getAccessory(Player player, Predicate<ItemStack> filter) {
        Optional<ICuriosItemHandler> itemHandler = CuriosApi.getCuriosInventory(player).resolve();
        if (itemHandler.isEmpty()) return ItemStack.EMPTY;

        Optional<SlotResult> result = itemHandler.get().findFirstCurio(filter);
        if (result.isEmpty()) return ItemStack.EMPTY;

        return result.get().stack();
    }
}
