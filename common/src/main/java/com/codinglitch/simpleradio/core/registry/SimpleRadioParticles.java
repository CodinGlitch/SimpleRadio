package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SimpleRadioParticles {
    public static Map<ResourceLocation, ParticleType<?>> PARTICLES = new HashMap<>();

    public static final SimpleParticleType SPEAK_RING = register(CommonSimpleRadio.id("speak_ring"), new SimpleParticleType(true));
    public static final SimpleParticleType SPEAK_LINE = register(CommonSimpleRadio.id("speak_line"), new SimpleParticleType(true));
    public static final SimpleParticleType LISTEN = register(CommonSimpleRadio.id("listen"), new SimpleParticleType(true));

    private static <T extends ParticleType<?>> T register(ResourceLocation location, T type) {
        PARTICLES.put(location, type);
        return type;
    }
}
