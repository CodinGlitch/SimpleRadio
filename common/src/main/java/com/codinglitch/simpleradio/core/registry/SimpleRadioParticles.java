package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class SimpleRadioParticles {
    public static Map<ResourceLocation, ParticleType<?>> PARTICLES = new HashMap<>();

    public static final SimpleParticleType SPEAK = register(CommonSimpleRadio.id("speak"), new SimpleParticleType(true));

    private static <T extends ParticleType<?>> T register(ResourceLocation location, T type) {
        PARTICLES.put(location, type);
        return type;
    }
}
