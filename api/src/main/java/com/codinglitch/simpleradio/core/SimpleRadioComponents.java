package com.codinglitch.simpleradio.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleRadioComponents {
    public static final Map<ResourceLocation, DataComponentType<?>> COMPONENT_TYPES = new LinkedHashMap<>();

    static PrimitiveCodec<UUID> UUID = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<UUID> read(final DynamicOps<T> ops, final T input) {
            return ops
                    .getStringValue(input)
                    .map(java.util.UUID::fromString);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final UUID value) {
            return ops.createString(value.toString());
        }

        @Override
        public String toString() {
            return "UUID";
        }
    };

    public static final DataComponentType<String> FREQUENCY = register(
            "frequency", DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<String> MODULATION = register(
            "modulation", DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<UUID> REFERENCE = register(
            "reference", DataComponentType.<UUID>builder().persistent(UUID).build()
    );

    public static final DataComponentType<Boolean> ACTIVATED = register(
            "activated", DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );

    public static final DataComponentType<String> MODULE = register(
            "module", DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<UUID> WIRE_TARGET = register(
            "wire_target", DataComponentType.<UUID>builder().persistent(UUID).build()
    );

    public static final DataComponentType<Long> WIRE_POSITION = register(
            "wire_position", DataComponentType.<Long>builder().persistent(Codec.LONG).build()
    );

    private static <T> DataComponentType<T> register(String location, DataComponentType<T> componentType) {
        COMPONENT_TYPES.put(ResourceLocation.fromNamespaceAndPath("simpleradio", location), componentType);
        return componentType;
    }
}
