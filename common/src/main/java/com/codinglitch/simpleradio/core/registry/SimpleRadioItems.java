package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.items.RadioItem;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.*;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioItems {
    public static final Map<ResourceLocation, Item> ITEMS = new HashMap<>();
    public static final Map<ResourceLocation, List<Item>> TAB_ITEMS = new HashMap<>();

    public static Item TRANSCEIVER = register(id("transceiver"), new TransceiverItem(new Item.Properties().stacksTo(1)));
    public static Item RADIOSMITHER = register(id("radiosmither"), new BlockItem(SimpleRadioBlocks.RADIOSMITHER, new Item.Properties()));
    public static Item RADIO = register(id("radio"), new RadioItem(new Item.Properties().stacksTo(1)));


    public static Item TRANSMITTING_MODULE = register(id("transmitting_module"), new Item(new Item.Properties()));
    public static Item RECEIVING_MODULE = register(id("receiving_module"), new Item(new Item.Properties()));
    public static Item SPEAKER_MODULE = register(id("speaker_module"), new Item(new Item.Properties()));

    private static Item register(ResourceLocation location, Item item) {
        return register(location, item, SimpleRadioMenus.RADIO_TAB_LOCATION);
    }

    private static Item register(ResourceLocation location, Item item, ResourceLocation tab) {
        if (tab != null) {
            TAB_ITEMS.computeIfAbsent(tab, key -> new ArrayList<>());
            TAB_ITEMS.get(tab).add(item);
        }

        ITEMS.put(location, item);
        return item;
    }
}
