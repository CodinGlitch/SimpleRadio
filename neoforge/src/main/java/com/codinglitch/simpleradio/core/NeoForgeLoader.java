package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.core.networking.SimpleRadioNetworking;
import com.codinglitch.simpleradio.core.registry.*;
import com.codinglitch.simpleradio.datagen.SimpleRadioBlockLootTableProvider;
import com.codinglitch.simpleradio.datagen.SimpleRadioRecipeProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoForgeLoader {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(
                event.includeServer(),
                new SimpleRadioRecipeProvider(generator.getPackOutput(), event.getLookupProvider())
        );

        generator.addProvider(
                event.includeServer(),
                new LootTableProvider(generator.getPackOutput(), Set.of(), List.of(
                        new LootTableProvider.SubProviderEntry(SimpleRadioBlockLootTableProvider::new, LootContextParamSets.BLOCK)
                ))
        );
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> SimpleRadioItems.ITEMS.forEach((location, itemHolder) -> helper.register(location, itemHolder.get())));
        event.register(Registries.BLOCK, helper -> SimpleRadioBlocks.BLOCKS.forEach((helper::register)));

        event.register(Registries.ENTITY_TYPE, helper -> SimpleRadioEntities.ENTITIES.forEach((helper::register)));
        event.register(Registries.BLOCK_ENTITY_TYPE, helper -> SimpleRadioBlockEntities.BLOCK_ENTITIES.forEach((helper::register)));

        event.register(Registries.MENU, helper -> SimpleRadioMenus.MENUS.forEach(helper::register));
        event.register(Registries.CREATIVE_MODE_TAB, helper -> SimpleRadioMenus.CREATIVE_TABS.forEach(helper::register));

        event.register(Registries.PARTICLE_TYPE, helper -> SimpleRadioParticles.PARTICLES.forEach(helper::register));

        event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, helper -> {
            helper.register(CommonSimpleRadio.id("items_enabled"), ItemsEnabledCondition.CODEC);
        });

        CommonSimpleRadio.load();
    }

    public static void loadItems() {

    }

    @SubscribeEvent
    public static void loadPackets(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(CommonSimpleRadio.ID);

        SimpleRadioNetworking.loadServerbound(new SimpleRadioNetworking.ServerboundRegistry() {
            @Override
            public <P extends CustomPacket> void register(ResourceLocation id, Class<P> packetClass, BiConsumer<P, FriendlyByteBuf> writer, TriConsumer<P, MinecraftServer, ServerPlayer> handler, FriendlyByteBuf.Reader<P> reader) {
                registrar.play(id, reader, payloadHandler -> payloadHandler
                    .server((packet, context) -> {
                        Player player = context.player().orElse(null);
                        if (!(player instanceof ServerPlayer serverPlayer)) return;
                        handler.accept(packet, serverPlayer.getServer(), serverPlayer);
                    }));
            }
        });

        SimpleRadioNetworking.loadClientbound(new SimpleRadioNetworking.ClientboundRegistry() {
            @Override
            public <P extends CustomPacket> void register(ResourceLocation id, Class<P> packetClass, BiConsumer<P, FriendlyByteBuf> writer, Consumer<P> handler, FriendlyByteBuf.Reader<P> reader) {
                registrar.play(id, reader, payloadHandler -> payloadHandler
                    .client((packet, context) -> handler.accept(packet)));
            }
        });
    }

    public static void load() {
        loadItems();
    }
}
