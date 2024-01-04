package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.world.inventory.MenuType;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioMenus {
    public static MenuType<RadiosmitherMenu> RADIOSMITHER_MENU;
    static {
        RADIOSMITHER_MENU = Services.REGISTRY.registerMenu(id("radiosmither"), RadiosmitherMenu::new);
    }
}
