package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.screens.RadiosmitherScreen;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import com.codinglitch.simpleradio.platform.ClientServices;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

public class CommonSimpleRadioClient {
    // -- Model Properties -- \\
    public static void loadProperties(TriConsumer<Item, ResourceLocation, ClampedItemPropertyFunction> registry) {
        registry.accept(SimpleRadioItems.TRANSCEIVER, new ResourceLocation("using"),
                (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);

        registry.accept(SimpleRadioItems.TRANSCEIVER, new ResourceLocation("speaking"),
                (stack, level, entity, i) -> entity != null && false ? 1.0f : 0.0f);
    }

    // -- Render Types -- \\
    public static void loadRenderTypes(BiConsumer<Block, RenderType> registry) {
        registry.accept(SimpleRadioBlocks.RADIOSMITHER, RenderType.cutout());
    }

    // -- Screens -- \\
    public static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void loadScreens() {
        ClientServices.REGISTRY.registerScreen(SimpleRadioMenus.RADIOSMITHER_MENU, RadiosmitherScreen::new);
    }

    public static void initialize() {
        loadScreens();
    }
}
