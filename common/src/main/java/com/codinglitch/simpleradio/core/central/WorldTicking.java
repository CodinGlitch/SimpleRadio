package com.codinglitch.simpleradio.core.central;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

public interface WorldTicking {
    void worldTick(ItemEntity item, Level level);
}
