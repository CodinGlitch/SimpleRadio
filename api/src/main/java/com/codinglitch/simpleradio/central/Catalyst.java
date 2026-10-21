package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.core.SimpleRadioComponents;
import com.codinglitch.simpleradio.radio.Source;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public record Catalyst(ResourceLocation location, Item associate, float efficiency) {

    public Source acceptSource(Source source) {
        source.addPower(source.getPower() * (efficiency - 1));
        return source;
    }
}
