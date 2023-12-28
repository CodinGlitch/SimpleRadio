package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class FabricLoader {
    public static void loadItems() {
        SimpleRadioItems.ITEMS.forEach(((location, item) -> Registry.register(BuiltInRegistries.ITEM, location, item)));
    }

    public static void load() {
        loadItems();
    }
}
