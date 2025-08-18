package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.client.core.registry.SimpleRadioModels;
import com.codinglitch.simpleradio.client.core.registry.models.InsulatorModel;
import com.codinglitch.simpleradio.client.core.registry.models.MicrophoneModel;
import com.codinglitch.simpleradio.client.core.registry.models.RadioModel;
import com.codinglitch.simpleradio.client.core.registry.renderers.*;
import com.codinglitch.simpleradio.client.core.registry.screens.RadiosmitherScreen;
import com.codinglitch.simpleradio.core.registry.*;
import com.codinglitch.simpleradio.core.registry.particles.ListenParticle;
import com.codinglitch.simpleradio.core.registry.particles.SpeakLineParticle;
import com.codinglitch.simpleradio.core.registry.particles.SpeakRingParticle;
import com.codinglitch.simpleradio.platform.ClientServices;
import com.codinglitch.simpleradio.routers.Receiver;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class CommonSimpleRadioClient {
    // -- Model Properties -- \\
    public static final Map<UUID, Boolean> isTransmitting = new HashMap<>();
    public static void loadProperties(TriConsumer<Item, ResourceLocation, ClampedItemPropertyFunction> registry) {
        registry.accept(SimpleRadioItems.TRANSCEIVER, new ResourceLocation("using"),
                (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);

        registry.accept(SimpleRadioItems.TRANSCEIVER, new ResourceLocation("speaking"),
            (stack, level, entity, i) -> {
                CompoundTag tag = stack.getOrCreateTag();
                if (!tag.contains("user")) return 0;

                UUID uuid = tag.getUUID("user");
                Receiver receiver = ClientRadioManager.getInstance().getReceiver(uuid);
                if (receiver == null) return 0;

                return receiver.getActivityTime() > 0 ? 1 : 0;
            }
        );

        registry.accept(SimpleRadioItems.WALKIE_TALKIE, new ResourceLocation("using"),
                (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);
        registry.accept(SimpleRadioItems.SPUDDIE_TALKIE, new ResourceLocation("using"),
                (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);
    }

    // -- Render Types -- \\
    public static void loadRenderTypes(BiConsumer<Block, RenderType> registry) {
        registry.accept(SimpleRadioBlocks.RADIOSMITHER, RenderType.cutout());
        registry.accept(SimpleRadioBlocks.ANTENNA, RenderType.cutout());
        registry.accept(SimpleRadioBlocks.RECEIVER, RenderType.cutout());
    }

    // -- Layer Definitions -- \\
    public static void loadLayerDefinitions(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registry) {
        registry.accept(RadioModel.LAYER_LOCATION, RadioModel::createBodyLayer);
        registry.accept(MicrophoneModel.LAYER_LOCATION, MicrophoneModel::createBodyLayer);
        registry.accept(InsulatorModel.LAYER_LOCATION, InsulatorModel::createBodyLayer);
    }

    // -- Entity Renderers -- \\
    public interface BlockEntityRendererRegistry {
        <BE extends BlockEntity> void register(BlockEntityType<BE> type, BlockEntityRendererProvider<? super BE> factory);
    }
    public static void loadBlockEntityRenderers(BlockEntityRendererRegistry registry) {
        registry.register(SimpleRadioBlockEntities.RADIO, RadioRenderer::new);
        registry.register(SimpleRadioBlockEntities.FREQUENCER, FrequencerRenderer::new);
        registry.register(SimpleRadioBlockEntities.MICROPHONE, MicrophoneRenderer::new);
        registry.register(SimpleRadioBlockEntities.INSULATOR, InsulatorRenderer::new);

        registry.register(SimpleRadioBlockEntities.TRANSMITTER, TransmitterRenderer::new);
        registry.register(SimpleRadioBlockEntities.RECEIVER, ReceiverRenderer::new);
    }

    public interface EntityRendererRegistry {
        <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<? super E> factory);
    }
    public static void loadEntityRenderers(EntityRendererRegistry registry) {
        registry.register(SimpleRadioEntities.WIRE, WireRenderer::new);
    }

    // -- Screens -- \\
    public static void loadScreens() {
        ClientServices.REGISTRY.registerScreen(SimpleRadioMenus.RADIOSMITHER_MENU, RadiosmitherScreen::new);
    }

    // -- Particles -- \\
    @FunctionalInterface
    public interface ParticleProviderRegistry {
        <O extends ParticleOptions> void register(ParticleType<O> type, ParticleEngine.SpriteParticleRegistration<O> registration);
    }
    public static void loadParticles(ParticleProviderRegistry registry) {
        registry.register(SimpleRadioParticles.SPEAK_RING, SpeakRingParticle.Provider::new);
        registry.register(SimpleRadioParticles.SPEAK_LINE, SpeakLineParticle.Provider::new);
        registry.register(SimpleRadioParticles.LISTEN, ListenParticle.Provider::new);
    }

    // -- Atlases -- \\
    public static void loadAtlases(BiConsumer<ResourceLocation, Supplier<LayerDefinition>> registry) {

    }

    public static void initialize() {
        SimpleRadioModels.load();
    }
}
