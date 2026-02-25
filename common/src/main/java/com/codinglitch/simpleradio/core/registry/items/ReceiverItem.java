package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.central.Frequencing;
import com.codinglitch.simpleradio.central.Module;
import com.codinglitch.simpleradio.core.central.Alterable;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioModules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReceiverItem extends BlockItem implements Frequencing, Alterable {
    public ReceiverItem(Properties settings) {
        super(SimpleRadioBlocks.RECEIVER, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltip) {
        appendTooltip(stack, components);
        super.appendHoverText(stack, context, components, tooltip);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);

        tick(stack, level);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        return super.place(context);
    }

    @Override
    public boolean canAcceptUpgrade(Module upgrade) {
        return upgrade == SimpleRadioModules.RANGE;
    }
}
