package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.core.networking.SimpleRadioNetworking;
import com.codinglitch.simpleradio.core.registry.*;
import com.codinglitch.simpleradio.datagen.SimpleRadioBlockLootTableProvider;
import com.codinglitch.simpleradio.datagen.SimpleRadioRecipeProvider;
import com.codinglitch.simpleradio.gametest.SimpleRadioTests;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeLoader {
    private static final String PROTOCOL_VERSION = "0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CommonSimpleRadio.id("channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(
                event.includeServer(),
                new SimpleRadioRecipeProvider(generator.getPackOutput())
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
        event.register(ForgeRegistries.Keys.ITEMS, helper -> SimpleRadioItems.ITEMS.forEach((location, itemHolder) -> helper.register(location, itemHolder.get())));
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> SimpleRadioBlocks.BLOCKS.forEach((helper::register)));

        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> SimpleRadioEntities.ENTITIES.forEach((helper::register)));
        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> SimpleRadioBlockEntities.BLOCK_ENTITIES.forEach((helper::register)));

        event.register(ForgeRegistries.Keys.MENU_TYPES, helper -> SimpleRadioMenus.MENUS.forEach(helper::register));
        event.register(Registries.CREATIVE_MODE_TAB, helper -> SimpleRadioMenus.CREATIVE_TABS.forEach(helper::register));

        event.register(ForgeRegistries.Keys.PARTICLE_TYPES, helper -> SimpleRadioParticles.PARTICLES.forEach(helper::register));

        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, helper -> {
            CraftingHelper.register(ItemsEnabledCondition.Serializer.INSTANCE);
        });

        CommonSimpleRadio.load();
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(SimpleRadioTests.class);
    }

    public static void loadPackets() {
        AtomicInteger index = new AtomicInteger();

        SimpleRadioNetworking.loadServerbound(new SimpleRadioNetworking.ServerboundRegistry() {
            @Override
            public <P extends CustomPacket> void register(ResourceLocation id, Class<P> packetClass, FriendlyByteBuf.Reader<P> reader, BiConsumer<P, FriendlyByteBuf> writer, TriConsumer<P, MinecraftServer, ServerPlayer> handler) {
                CHANNEL.messageBuilder(packetClass, index.getAndIncrement())
                        .decoder(reader).encoder(writer)
                        .consumerMainThread((packet, context) -> {
                            handler.accept(packet, context.get().getSender().getServer(), context.get().getSender());
                            context.get().setPacketHandled(true);
                        }).add();
            }
        });

        SimpleRadioNetworking.loadClientbound(new SimpleRadioNetworking.ClientboundRegistry() {
            @Override
            public <P extends CustomPacket> void register(ResourceLocation id, Class<P> packetClass, FriendlyByteBuf.Reader<P> reader, BiConsumer<P, FriendlyByteBuf> writer, Consumer<P> handler) {
                CHANNEL.messageBuilder(packetClass, index.getAndIncrement())
                        .decoder(reader).encoder(writer)
                        .consumerMainThread((packet, context) -> {
                            handler.accept(packet);
                            context.get().setPacketHandled(true);
                        }).add();
            }
        });
    }

    public static void loadItems() {

    }

    public static void load() {
        loadItems();
        loadPackets();
    }

    public static void loadClient() {
    }
}
