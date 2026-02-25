package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleRadioComponents {
    public static final Map<ResourceLocation, DataComponentType<?>> COMPONENT_TYPES = new LinkedHashMap<>();

    public static final DataComponentType<String> FREQUENCY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            CommonSimpleRadio.id("frequency"),
            DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<String> MODULATION = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            CommonSimpleRadio.id("modulation"),
            DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<String> REFERENCE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            CommonSimpleRadio.id("reference"),
            DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<Boolean> ACTIVATED = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            CommonSimpleRadio.id("activated"),
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );

    public static final DataComponentType<String> MODULE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            CommonSimpleRadio.id("module"),
            DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    private static <T> DataComponentType<T> register(ResourceLocation location, DataComponentType<T> componentType) {
        COMPONENT_TYPES.put(location, componentType);
        return componentType;
    }
}
