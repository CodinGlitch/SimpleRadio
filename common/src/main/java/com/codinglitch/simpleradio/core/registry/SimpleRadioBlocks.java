package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.blocks.RadiosmitherBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;

public class SimpleRadioBlocks {
    public static final HashMap<ResourceLocation, Block> BLOCKS = new HashMap<>();

    public static Block RADIOSMITHER = register(id("radiosmither"), new RadiosmitherBlock(Block.Properties.of()));

    private static ResourceLocation id(String id) {
        return new ResourceLocation(CommonSimpleRadio.ID, id);
    }
    private static Block register(ResourceLocation location, Block block) {
        BLOCKS.put(location, block);
        return block;
    }
}
