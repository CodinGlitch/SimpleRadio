package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioItems {
    public static final HashMap<ResourceLocation, Item> ITEMS = new HashMap<>();

    public static Item TRANSCEIVER = register(id("transceiver"), new TransceiverItem(new Item.Properties()));
    public static Item RADIOSMITHER = register(id("radiosmither"), new BlockItem(SimpleRadioBlocks.RADIOSMITHER, new Item.Properties()));

    private static Item register(ResourceLocation location, Item item) {
        ITEMS.put(location, item);
        return item;
    }
}
