package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.common.crafting.conditions.ICondition;

public record ItemsEnabledCondition(String item) implements ICondition {
    public static final Codec<ItemsEnabledCondition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("item").forGetter(ItemsEnabledCondition::item)
    ).apply(builder, ItemsEnabledCondition::new));

    @Override
    public boolean test(ICondition.IContext context) {
        return SimpleRadioItems.getByName(item).enabled;
    }

    @Override
    public String toString() {
        return "item_enabled(\"" + item + "\")";
    }

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }
}