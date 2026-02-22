package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Catalyst;
import com.codinglitch.simpleradio.core.registry.catalysts.GildedBlackstoneCatalyst;

public class SimpleRadioCatalysts {
    public static Catalyst GILDED_BLACKSTONE = CatalystRegistry.register(CommonSimpleRadio.id("catalyst/gilded_blackstone"),
            new GildedBlackstoneCatalyst().setEfficiency(0.9f)
    );

    public static void load() {}
}
