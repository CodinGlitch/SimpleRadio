package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.core.FabricLoader;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;

public class SimpleRadioClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader.loadClientPackets();

        CommonSimpleRadioClient.initialize();
        CommonSimpleRadioClient.loadProperties(ItemProperties::register);
        CommonSimpleRadioClient.loadRenderTypes(BlockRenderLayerMap.INSTANCE::putBlock);
    }
}
