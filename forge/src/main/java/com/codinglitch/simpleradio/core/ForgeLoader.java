package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.packets.*;
import com.codinglitch.simpleradio.core.registry.*;
import com.codinglitch.simpleradio.datagen.SimpleRadioBlockLootTableProvider;
import com.codinglitch.simpleradio.datagen.SimpleRadioRecipeProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
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
    public static final SimpleChannel CHANNEL = ChannelBuilder.named(CommonSimpleRadio.id("channel"))
            .optional()
            .networkProtocolVersion(0)
            .simpleChannel();

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

        event.register(ForgeRegistries.Keys.CONDITION_SERIALIZERS, helper -> {
            helper.register(CommonSimpleRadio.id("items_enabled"), ItemsEnabledCondition.CODEC);
        });

        CommonSimpleRadio.load();
    }

    public static void loadPackets() {
        AtomicInteger index = new AtomicInteger();

        CHANNEL.messageBuilder(ServerboundRadioUpdatePacket.class, index.getAndIncrement()).decoder(ServerboundRadioUpdatePacket::decode).encoder(ServerboundRadioUpdatePacket::encode)
                .consumerMainThread(serverbound(ServerboundRadioUpdatePacket::handle)).add();
        CHANNEL.messageBuilder(ServerboundRequestRouterPacket.class, index.getAndIncrement()).decoder(ServerboundRequestRouterPacket::decode).encoder(ServerboundRequestRouterPacket::encode)
                .consumerMainThread(serverbound(ServerboundRequestRouterPacket::handle)).add();

        CHANNEL.messageBuilder(ClientboundRegisterRouterPacket.class, index.getAndIncrement()).decoder(ClientboundRegisterRouterPacket::decode).encoder(ClientboundRegisterRouterPacket::encode)
                .consumerMainThread(clientbound(ClientboundRegisterRouterPacket::handle)).add();
        CHANNEL.messageBuilder(ClientboundActivityPacket.class, index.getAndIncrement()).decoder(ClientboundActivityPacket::decode).encoder(ClientboundActivityPacket::encode)
                .consumerMainThread(clientbound(ClientboundActivityPacket::handle)).add();
        CHANNEL.messageBuilder(ClientboundTransceiverPacket.class, index.getAndIncrement()).decoder(ClientboundTransceiverPacket::decode).encoder(ClientboundTransceiverPacket::encode)
                .consumerMainThread(clientbound(ClientboundTransceiverPacket::handle)).add();
        CHANNEL.messageBuilder(ClientboundWireEffectPacket.class, index.getAndIncrement()).decoder(ClientboundWireEffectPacket::decode).encoder(ClientboundWireEffectPacket::encode)
                .consumerMainThread(clientbound(ClientboundWireEffectPacket::handle)).add();

        CHANNEL.messageBuilder(ClientboundSpeakSoundPacket.class, index.getAndIncrement()).decoder(ClientboundSpeakSoundPacket::decode).encoder(ClientboundSpeakSoundPacket::encode)
                .consumerMainThread(clientbound(ClientboundSpeakSoundPacket::handle)).add();
    }

    private static <P> BiConsumer<P, CustomPayloadEvent.Context> serverbound(TriConsumer<P, MinecraftServer, ServerPlayer> consumer) {
        return (packet, context) -> {
            consumer.accept(packet, context.getSender().getServer(), context.getSender());
            context.setPacketHandled(true);
        };
    }
    public static <P> BiConsumer<P, CustomPayloadEvent.Context> clientbound(Consumer<P> consumer) {
        return (packet, context) -> {
            consumer.accept(packet);
            context.setPacketHandled(true);
        };
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
