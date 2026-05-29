package com.codinglitch.simpleradio;


import com.codinglitch.simpleradio.core.NeoForgeLoader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CommonSimpleRadio.ID)
public class SimpleRadio {

    public SimpleRadio(IEventBus modBus) {
        CommonSimpleRadio.initialize();

        NeoForgeLoader.load(modBus);
    }
}