package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.central.Catalyst;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalystRegistry {
    private static final HashMap<ResourceLocation, Catalyst> CATALYSTS = new HashMap<>();

    public static List<Catalyst> getCatalysts() {
        return CATALYSTS.values().stream().toList();
    }

    public static Catalyst get(ResourceLocation location) {
        return CATALYSTS.get(location);
    }

    public static Catalyst get(Item item) {
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
}
