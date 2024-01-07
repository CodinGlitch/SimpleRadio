package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

import java.util.HashMap;
import java.util.Map;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioMenus {
    public static Map<ResourceLocation, MenuType<?>> MENUS = new HashMap<>();

    public static MenuType<RadiosmitherMenu> RADIOSMITHER_MENU;
    static {
        RADIOSMITHER_MENU = Services.REGISTRY.registerMenu(id("radiosmither"), RadiosmitherMenu::new);
    }

    public static void load() {}
}
