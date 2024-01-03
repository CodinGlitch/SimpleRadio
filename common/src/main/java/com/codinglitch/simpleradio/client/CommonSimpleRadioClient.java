package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

public class CommonSimpleRadioClient {
    public static void loadProperties(TriConsumer<Item, ResourceLocation, ClampedItemPropertyFunction> registry) {
        registry.accept(SimpleRadioItems.TRANSCEIVER, new ResourceLocation("using"),
                (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);

        registry.accept(SimpleRadioItems.TRANSCEIVER, new ResourceLocation("speaking"),
                (stack, level, entity, i) -> entity != null && false ? 1.0f : 0.0f);
    }

    public static void loadRenderTypes(BiConsumer<Block, RenderType> registry) {
        registry.accept(SimpleRadioBlocks.RADIOSMITHER, RenderType.cutout());
    }

    public static void initialize() {

    }
}
