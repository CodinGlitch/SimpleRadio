package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

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

    public static final ResourceLocation RADIO_TAB_LOCATION = id("simple_radio_tab");
    public static final CreativeModeTab RADIO_TAB = Services.REGISTRY.registerCreativeTab(RADIO_TAB_LOCATION, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
            .title(Component.translatable("item_group." + CommonSimpleRadio.ID))
            .icon(() -> new ItemStack(SimpleRadioItems.TRANSCEIVER))
            .displayItems((params, output) -> SimpleRadioItems.TAB_ITEMS.get(RADIO_TAB_LOCATION).forEach(output::accept))
            .build()
    );

    public static void load() {}
}
