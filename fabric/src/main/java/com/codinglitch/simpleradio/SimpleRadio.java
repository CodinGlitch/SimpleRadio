package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.core.FabricLoader;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.blocks.RadiosmitherBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SimpleRadio implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        CommonSimpleRadio.info("Hello Fabric world!");
        CommonSimpleRadio.initialize();

        FabricLoader.load();
    }
}
