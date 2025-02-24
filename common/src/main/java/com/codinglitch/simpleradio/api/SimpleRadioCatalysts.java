package com.codinglitch.simpleradio.api;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.Catalyst;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class SimpleRadioCatalysts {
    private static final HashMap<ResourceLocation, Catalyst> CATALYSTS = new HashMap<>();

    public static Catalyst GILDED_BLACKSTONE = register(CommonSimpleRadio.id("catalyst/gilded_blackstone"),
            new Catalyst(Items.GILDED_BLACKSTONE)
    );

    public static Catalyst fromLocation(ResourceLocation location) {
        for (Map.Entry<ResourceLocation, Catalyst> entry : CATALYSTS.entrySet()) {
            if (entry.getKey().equals(location)) return entry.getValue();
        }

        return null;
    }

    public static Catalyst fromItem(Item item) {
        for (Map.Entry<ResourceLocation, Catalyst> entry : CATALYSTS.entrySet()) {
            Catalyst catalyst = entry.getValue();
            if (catalyst.associate == item) return catalyst;
        }

        return null;
    }

    public static Catalyst register(ResourceLocation location, Catalyst catalyst) {
        catalyst.location = location;

        CATALYSTS.put(location, catalyst);
        return catalyst;
    }

    public static void load() {}
}
