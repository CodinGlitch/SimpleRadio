package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.client.core.SimpleRadioArmPoses;
import com.codinglitch.simpleradio.compat.CuriosCompat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, modid = CommonSimpleRadio.ID)
public class SimpleRadioClient {
    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        CommonSimpleRadioClient.loadLayerDefinitions(event::registerLayerDefinition);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        CommonSimpleRadioClient.loadBlockEntityRenderers(event::registerBlockEntityRenderer);
        CommonSimpleRadioClient.loadEntityRenderers(event::registerEntityRenderer);
    }

    @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        CommonSimpleRadioClient.loadParticles(event::registerSpriteSet);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        CommonSimpleRadioClient.loadScreens(event::register);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        for (Keybinds.Binding binding : Keybinds.BINDINGS) {
            Keybinds.processBinding(binding);
        }
    }

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        Keybinds.register(event::register);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CommonSimpleRadioClient.initialize();
        CommonSimpleRadioClient.loadProperties(ItemProperties::register);

        if (CompatCore.CURIOS.isLoaded()) {
            CuriosCompat.initialize();
        }

        NeoForge.EVENT_BUS.addListener(SimpleRadioClient::onClientTick);
    }

    public static EnumProxy<HumanoidModel.ArmPose> loadPose(SimpleRadioArmPoses.Pose pose) {
        SimpleRadioArmPoses.Transform transform = pose.transform();
        return new EnumProxy<>(
                HumanoidModel.ArmPose.class, pose.twoHanded(),
                (IArmPoseTransformer) transform::apply
        );
    }

    public static final EnumProxy<HumanoidModel.ArmPose> HOLD_LAPEL_ENUM_PROXY = loadPose(SimpleRadioArmPoses.HOLD_LAPEL);

}
