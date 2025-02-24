package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.CatalystRegistry;
import com.codinglitch.simpleradio.api.central.Catalyst;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class SimpleRadioCatalysts {
    public static Catalyst GILDED_BLACKSTONE = CatalystRegistry.register(CommonSimpleRadio.id("catalyst/gilded_blackstone"),
            new Catalyst(Items.GILDED_BLACKSTONE)
    );

    public static void load() {}
}
