package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.radio.AudioEffect;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class AudioEffectRegistry {
    private static final HashMap<ResourceLocation, AudioEffectLoader<?>> EFFECTS = new HashMap<>();

    public static List<AudioEffectLoader<?>> getEffects() {
        return EFFECTS.values().stream().toList();
    }

    public static AudioEffectLoader<?> get(ResourceLocation location) {
        return EFFECTS.get(location);
    }

    public static <E extends AudioEffect> AudioEffectLoader<E> register(ResourceLocation location, Supplier<E> maker) {
        AudioEffectLoader<E> loader = new AudioEffectLoader<>(maker);

        EFFECTS.put(location, loader);
        return loader;
    }

    public static class AudioEffectLoader<E extends AudioEffect> {
        private final Supplier<E> maker;

        public AudioEffectLoader(Supplier<E> maker) {
            this.maker = maker;
        }

        public E make() {
            return maker.get();
        }
    }
}
