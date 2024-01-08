package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.core.central.Receiving;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadioItem extends BlockItem implements Receiving {
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
