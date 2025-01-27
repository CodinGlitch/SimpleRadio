package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.HashMap;
import java.util.Map;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioEntities {
    public static Map<ResourceLocation, EntityType<?>> ENTITIES = new HashMap<>();

    public static final EntityType<Wire> WIRE = Services.REGISTRY.registerEntity(
            Wire::new, MobCategory.MISC,
            wireBuilder -> wireBuilder.sized(0.5f, 0.5f),
            id("wire")
    );

    public static void load() {}
}
