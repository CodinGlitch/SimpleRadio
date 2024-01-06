package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SimpleRadioClient {
    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        CommonSimpleRadioClient.loadLayerDefinitions(event::registerLayerDefinition);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CommonSimpleRadioClient.initialize();
        CommonSimpleRadioClient.loadProperties(ItemProperties::register);
    }
}
