package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.core.FabricLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class SimpleRadioClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader.loadClientPackets();

        CommonSimpleRadioClient.initialize();
        CommonSimpleRadioClient.loadScreens(MenuScreens::register);
        CommonSimpleRadioClient.loadProperties(ItemProperties::register);
        CommonSimpleRadioClient.loadRenderTypes(BlockRenderLayerMap.INSTANCE::putBlock);
        CommonSimpleRadioClient.loadLayerDefinitions((location, definition) -> EntityModelLayerRegistry.registerModelLayer(location, definition::get));
        CommonSimpleRadioClient.loadBlockEntityRenderers(BlockEntityRenderers::register);
        CommonSimpleRadioClient.loadEntityRenderers(EntityRendererRegistry::register);
        CommonSimpleRadioClient.loadParticles(new CommonSimpleRadioClient.ParticleProviderRegistry() {
            @Override
            public <O extends ParticleOptions> void register(ParticleType<O> type, ParticleEngine.SpriteParticleRegistration<O> registration) {
                ParticleFactoryRegistry.getInstance().register(type, registration::create);
            }
        });

    }
}
