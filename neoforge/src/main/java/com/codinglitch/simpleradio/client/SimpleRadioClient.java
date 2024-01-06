package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.platform.NeoForgeClientRegistryHelper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SimpleRadioClient {
    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        CommonSimpleRadioClient.loadLayerDefinitions(event::registerLayerDefinition);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        CommonSimpleRadioClient.loadBlockEntityRenderers(event::registerBlockEntityRenderer);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CommonSimpleRadioClient.initialize();
        CommonSimpleRadioClient.loadProperties(ItemProperties::register);

        event.enqueueWork(CommonSimpleRadioClient::loadScreens);
    }
}
