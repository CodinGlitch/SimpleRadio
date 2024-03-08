package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.core.registry.items.UpgradeModuleItem;
import com.codinglitch.simpleradio.radio.RadioChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public interface Upgradable {
    boolean canAcceptUpgrade(UpgradeModuleItem.Type type);
    default boolean canAcceptUpgrade(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof UpgradeModuleItem upgradeModuleItem)
            return canAcceptUpgrade(upgradeModuleItem.getType());
        return false;
    };
}
