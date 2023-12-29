package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.core.networking.packets.ClientboundRadioPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class FabricLoader {
    public static void loadItems() {
        SimpleRadioItems.ITEMS.forEach(((location, item) -> Registry.register(BuiltInRegistries.ITEM, location, item)));
    }

    public static void loadPackets() {

    }

    public static void loadClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundRadioPacket.ID,
                clientbound(ClientboundRadioPacket::decode, ClientboundRadioPacket::handle));
    }

    public static <P> ServerPlayNetworking.PlayChannelHandler serverbound(Function<FriendlyByteBuf, P> decoder, TriConsumer<P, MinecraftServer, ServerPlayer> consumer) {
        return (server, player, listener, buffer, sender) -> consumer.accept(decoder.apply(buffer), server, player);
    }
    public static <P> ClientPlayNetworking.PlayChannelHandler clientbound(Function<FriendlyByteBuf, P> decoder, Consumer<P> consumer) {
        return (client, listener, buffer, sender) -> consumer.accept(decoder.apply(buffer));
    }

    public static void load() {
        loadItems();
        loadPackets();
    }
}