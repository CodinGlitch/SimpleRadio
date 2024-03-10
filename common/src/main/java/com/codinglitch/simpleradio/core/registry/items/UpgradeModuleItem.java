package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.core.central.Receiving;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradeModuleItem extends TieredItem {
    public enum Type {
        RANGE,
        CLARITY,
        BATTERY,

    }

    private final Type type;

    public UpgradeModuleItem(Tier tier, Type type, Properties properties) {
        super(tier, properties);

        this.type = type;
    }

    public Type getType() {
        return this.type;
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltip) {

        super.appendHoverText(stack, level, components, tooltip);
    }
}
