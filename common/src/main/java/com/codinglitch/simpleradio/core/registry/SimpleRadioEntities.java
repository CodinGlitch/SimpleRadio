package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.core.registry.blocks.*;
import com.codinglitch.simpleradio.core.registry.entities.WireEntity;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioEntities {
    public static Map<ResourceLocation, EntityType<?>> ENTITIES = new HashMap<>();

    public static void load() {}
}
