package com.codinglitch.simpleradio.core.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleRadioComponents {
    public static final Map<ResourceLocation, DataComponentType<?>> COMPONENT_TYPES = new LinkedHashMap<>();



    private static <T> DataComponentType<T> register(ResourceLocation location, DataComponentType<T> componentType) {
        COMPONENT_TYPES.put(location, componentType);
        return componentType;
    }
}
