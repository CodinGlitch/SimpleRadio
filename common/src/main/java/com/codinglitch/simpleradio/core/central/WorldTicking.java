package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.core.registry.items.ModuleItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface WorldTicking {
    void worldTick(ItemEntity item, Level level);
}
