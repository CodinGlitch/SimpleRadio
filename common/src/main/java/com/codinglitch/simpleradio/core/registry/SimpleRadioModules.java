package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Module;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class SimpleRadioModules {
    private static final HashMap<ResourceLocation, Module> MODULES = new HashMap<>();

    public static final Module RANGE = register(CommonSimpleRadio.id("range"), new Module(
            CommonSimpleRadio.id("range"),
            Module.Type.TRANSMITTING, Module.Type.RECEIVING
    ));
    public static final Module CLARITY = register(CommonSimpleRadio.id("clarity"), new Module(
            CommonSimpleRadio.id("clarity"),
            Module.Type.EMITTING
    ));
    public static final Module BATTERY = register(CommonSimpleRadio.id("battery"), new Module(
            CommonSimpleRadio.id("battery"),
            Module.Type.POWERING
    ));
    public static final Module LATCH = register(CommonSimpleRadio.id("latch"), new Module(
            CommonSimpleRadio.id("latch"),
            Module.Type.LISTENING
    ));
    public static final Module SOLAR = register(CommonSimpleRadio.id("solar"), new Module(
            CommonSimpleRadio.id("solar"),
            Module.Type.POWERING
    ));

    public static Module register(ResourceLocation location, Module module) {
        MODULES.put(location, module);
        return module;
    }

    public static Module get(ResourceLocation identifier) {
        return MODULES.get(identifier);
    }

    public static Module get(String name) {
        return MODULES.get(CommonSimpleRadio.id(name));
    }

    public static void load() {}
}
