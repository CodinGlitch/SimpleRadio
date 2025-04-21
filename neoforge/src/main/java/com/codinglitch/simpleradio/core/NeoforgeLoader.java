package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.SimpleRadioNetworking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeoforgeLoader {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> SimpleRadioItems.ITEMS.forEach((location, itemHolder) -> helper.register(location, itemHolder.get())));
        event.register(Registries.BLOCK, helper -> SimpleRadioBlocks.BLOCKS.forEach((helper::register)));
        event.register(Registries.BLOCK_ENTITY_TYPE, helper -> SimpleRadioBlockEntities.BLOCK_ENTITIES.forEach(helper::register));
        event.register(Registries.MENU, helper -> SimpleRadioMenus.MENUS.forEach(helper::register));
        event.register(Registries.CREATIVE_MODE_TAB, helper -> SimpleRadioMenus.CREATIVE_TABS.forEach(helper::register));

        event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, helper -> {
            helper.register(CommonSimpleRadio.id("items_enabled"), ItemsEnabledCondition.CODEC);
        });
    }

    public static void loadItems() {

    }

    @SubscribeEvent
    public static void loadPackets(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(CommonSimpleRadio.ID);

        SimpleRadioNetworking.loadServerbound(new SimpleRadioNetworking.ServerboundRegistry() {
            @Override
            public <P extends CustomPacketPayload> void register(ResourceLocation id, FriendlyByteBuf.Reader<P> reader, BiConsumer<P, FriendlyByteBuf> writer, org.apache.logging.log4j.util.TriConsumer<P, MinecraftServer, ServerPlayer> handler) {
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
            public <P extends CustomPacketPayload> void register(ResourceLocation id, FriendlyByteBuf.Reader<P> reader, BiConsumer<P, FriendlyByteBuf> writer, Consumer<P> handler) {
                registrar.play(id, reader, payloadHandler -> payloadHandler
                    .client((packet, context) -> handler.accept(packet)));
            }
        });
    }

    public static void load() {
        loadItems();
    }
}
