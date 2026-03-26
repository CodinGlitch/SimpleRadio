package com.codinglitch.simpleradio;


import com.codinglitch.simpleradio.core.NeoForgeLoader;
import net.neoforged.fml.common.Mod;

@Mod(CommonSimpleRadio.ID)
public class SimpleRadio {

    public SimpleRadio() {
        CommonSimpleRadio.initialize();

        NeoForgeLoader.load();
    }
}