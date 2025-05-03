package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.radio.RadioSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class Catalyst {
    public ResourceLocation location;
    public final Item associate;

    public float efficiency = 1;

    public Catalyst(Item associate) {
        this.associate = associate;
    }

    public Catalyst setEfficiency(float efficiency) {
        this.efficiency = efficiency;
        return this;
    }

    public RadioSource acceptSource(RadioSource source) {
        source.transmissionPower *= efficiency;
        return source;
    }
}
