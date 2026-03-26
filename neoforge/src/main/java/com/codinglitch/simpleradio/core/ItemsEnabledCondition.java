package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;

public record ItemsEnabledCondition(String item) implements ICondition {
    public static final MapCodec<ItemsEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
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
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}