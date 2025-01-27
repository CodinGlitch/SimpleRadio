package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.RadioSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Catalyst {
    public ResourceLocation location;
    public final Item associate;

    public Catalyst(Item associate) {
        this.associate = associate;
    }

    public RadioSource acceptSource(RadioSource source) {
        return source;
    }
}
