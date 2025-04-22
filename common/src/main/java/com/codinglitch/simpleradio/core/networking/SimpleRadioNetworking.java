package com.codinglitch.simpleradio.core.networking;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.client.core.SimpleRadioClientNetworking;
import com.codinglitch.simpleradio.core.networking.packets.*;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleRadioNetworking {
    public interface ServerboundRegistry {
        <P extends CustomPacket> void register(
                ResourceLocation id, Class<P> packetClass,
                BiConsumer<P, FriendlyByteBuf> writer,
                TriConsumer<P, MinecraftServer, ServerPlayer> handler,
                FriendlyByteBuf.Reader<P> reader
        );
    }
    public interface ClientboundRegistry {
        <P extends CustomPacket> void register(
                ResourceLocation id, Class<P> packetClass,
                BiConsumer<P, FriendlyByteBuf> writer,
                Consumer<P> handler,
                FriendlyByteBuf.Reader<P> reader
        );
    }

    // ---- Packets ---- \\

    public static void loadServerbound(ServerboundRegistry registry) {
        registry.register(ServerboundRadioUpdatePacket.ID, ServerboundRadioUpdatePacket.class, ServerboundRadioUpdatePacket::write, SimpleRadioNetworking::handleRadioUpdate, ServerboundRadioUpdatePacket::read);
        registry.register(ServerboundRequestRouterPacket.ID, ServerboundRequestRouterPacket.class, ServerboundRequestRouterPacket::write, SimpleRadioNetworking::handleRequestRouter, ServerboundRequestRouterPacket::read);
    }

    public static void loadClientbound(ClientboundRegistry registry) {
        registry.register(ClientboundActivityPacket.ID, ClientboundActivityPacket.class, ClientboundActivityPacket::write, SimpleRadioClientNetworking::handleActivityPacket, ClientboundActivityPacket::read);
        registry.register(ClientboundRegisterRouterPacket.ID, ClientboundRegisterRouterPacket.class, ClientboundRegisterRouterPacket::write, SimpleRadioClientNetworking::handleRegisterRouter, ClientboundRegisterRouterPacket::read);
        registry.register(ClientboundSpeakSoundPacket.ID, ClientboundSpeakSoundPacket.class, ClientboundSpeakSoundPacket::write, SimpleRadioClientNetworking::handleSpeakSound, ClientboundSpeakSoundPacket::read);
        registry.register(ClientboundWireEffectPacket.ID, ClientboundWireEffectPacket.class, ClientboundWireEffectPacket::write, SimpleRadioClientNetworking::handleWireEffect, ClientboundWireEffectPacket::read);
    }

    // ---- Handlers ---- \\

    public static void handleRadioUpdate(ServerboundRadioUpdatePacket packet, MinecraftServer server, ServerPlayer player) {
        server.execute(() -> {
            if (!Frequency.check(packet.frequency())) return;

            AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof RadiosmitherMenu radiosmitherMenu) {
                if (!player.containerMenu.stillValid(player)) {
                    CommonSimpleRadio.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                    return;
                }

                radiosmitherMenu.updateTinkering(packet.frequency(), packet.modulation());
            }
        });
    }

    public static void handleRequestRouter(ServerboundRequestRouterPacket packet, MinecraftServer server, ServerPlayer player) {
        UUID reference = packet.reference();
        String type = packet.type();
        short mapping = packet.mapping();

        server.execute(() -> {
            short identifier = RadioManager.getIdentifier(r -> reference.equals(r.reference) && r.getClass().getSimpleName().equals(type));
            if (identifier == Short.MAX_VALUE) {
                CommonSimpleRadio.warn("We could not find the {} with reference {} for mapping {}!", type, reference, mapping);
                return;
            }

            Services.NETWORKING.sendToPlayer(player, new ClientboundRegisterRouterPacket(
                    mapping, identifier
            ));
        });
    }
}
