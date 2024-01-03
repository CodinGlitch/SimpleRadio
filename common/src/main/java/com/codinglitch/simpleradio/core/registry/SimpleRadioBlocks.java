package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.blocks.RadiosmitherBlock;
import com.codinglitch.simpleradio.core.registry.blocks.RadiosmitherBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioBlocks {
    public static final HashMap<ResourceLocation, Block> BLOCKS = new HashMap<>();

    public static Block RADIOSMITHER = register(id("radiosmither"), new RadiosmitherBlock(Block.Properties.of()));

    private static Block register(ResourceLocation location, Block block) {
        BLOCKS.put(location, block);
        return block;
    }
}
