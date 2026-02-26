package com.codinglitch.simpleradio.central;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public class Module {
    public ResourceLocation identifier;
    public ResourceLocation texture;
    public List<Type> types;

    public Module(ResourceLocation identifier) {
        this(identifier, ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), "module/"+identifier.getPath()));
    }
    public Module(ResourceLocation identifier, Type... types) {
        this(identifier, ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), "module/"+identifier.getPath()), types);
    }
    public Module(ResourceLocation identifier, ResourceLocation texture) {
        this(identifier, texture, Type.TRANSMITTING);
    }
    public Module(ResourceLocation identifier, ResourceLocation texture, Type... types) {
        this.identifier = identifier;
        this.texture = texture;
        this.types = Arrays.stream(types).toList();
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public enum Type {
        TRANSMITTING("transmitting"),
        RECEIVING("receiving"),
        EMITTING("emitting"),
        LISTENING("listening"),
        POWERING("powering");

        private final String name;
        public String getName() {
            return name;
        }

        Type(String name) {
            this.name = name;
        }
    }
}
