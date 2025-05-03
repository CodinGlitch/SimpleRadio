package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.core.registry.items.ModuleItem;
import net.minecraft.world.item.ItemStack;

public interface Alterable {
    boolean canAcceptUpgrade(Module upgrade);
    default boolean canAcceptUpgrade(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof ModuleItem moduleItem)
            return canAcceptUpgrade(moduleItem.getModule(stack));
        return false;
    };
}
