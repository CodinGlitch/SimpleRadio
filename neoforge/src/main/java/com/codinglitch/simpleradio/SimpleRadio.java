package com.codinglitch.simpleradio;


import com.codinglitch.simpleradio.core.NeoforgeLoader;
import net.neoforged.fml.common.Mod;

@Mod(CommonSimpleRadio.ID)
public class SimpleRadio {

    public SimpleRadio() {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        CommonSimpleRadio.info("Hello NeoForge world!");
        CommonSimpleRadio.initialize();

        NeoforgeLoader.load();
    }
}