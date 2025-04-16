package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.core.ForgeLoader;
import net.minecraftforge.fml.common.Mod;

@Mod(CommonSimpleRadio.ID)
public class SimpleRadio {
    
    public SimpleRadio() {
        CommonSimpleRadio.initialize();
        ForgeLoader.load();
    }
}