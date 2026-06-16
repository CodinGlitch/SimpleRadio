package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.core.FabricLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.impl.client.particle.ParticleFactoryRegistryImpl;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import java.util.function.Function;

public class SimpleRadioClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader.loadClientPackets();

        CommonSimpleRadioClient.initialize();
        CommonSimpleRadioClient.loadScreens();
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

        for (Keybinds.Binding binding : Keybinds.BINDINGS) {
            KeyBindingHelper.registerKeyBinding(binding.mapping);
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Keybinds.Binding binding : Keybinds.BINDINGS) {
                Keybinds.processBinding(binding);
            }
        });

    }
}
