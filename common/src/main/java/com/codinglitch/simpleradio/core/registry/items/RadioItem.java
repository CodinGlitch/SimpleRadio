package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.Transceiving;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RadioItem extends BlockItem implements Transceiving {
    public RadioItem(Properties settings) {
        super(SimpleRadioBlocks.RADIO, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltip) {
        appendTooltip(stack, components);
        super.appendHoverText(stack, level, components, tooltip);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);

        tick(stack, level, entity);
    }
}
