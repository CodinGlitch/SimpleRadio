package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioMenus {
    public static Map<ResourceLocation, MenuType<?>> MENUS = new HashMap<>();
    public static Map<ResourceLocation, CreativeModeTab> CREATIVE_TABS = new HashMap<>();

    public static MenuType<RadiosmitherMenu> RADIOSMITHER_MENU;
    static {
        RADIOSMITHER_MENU = Services.REGISTRY.registerMenu(id("radiosmither"), RadiosmitherMenu::new);
    }

    public static final ResourceLocation RADIO_TAB_LOCATION = CommonSimpleRadio.id("simple_radio"); // hack
    public static final CreativeModeTab RADIO_TAB = Services.REGISTRY.registerCreativeTab(RADIO_TAB_LOCATION, new CreativeModeTab(-1, String.format("%s.%s", RADIO_TAB_LOCATION.getNamespace(), RADIO_TAB_LOCATION.getPath())) {
        public ItemStack makeIcon() {
            return new ItemStack(SimpleRadioItems.TRANSCEIVER);
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            super.fillItemList(items);

            SimpleRadioItems.ITEMS.entrySet().stream().filter(entry -> RADIO_TAB_LOCATION.equals(entry.getValue().tab))
                    .forEach(entry -> { if (entry.getValue().enabled) items.add(new ItemStack(entry.getValue().get())); });
        }
    });

    public static void load() {}
}
