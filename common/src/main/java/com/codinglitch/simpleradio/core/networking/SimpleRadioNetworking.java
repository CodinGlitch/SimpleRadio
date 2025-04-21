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
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleRadioNetworking {
    public interface ServerboundRegistry {
        <P extends CustomPacketPayload> void register(
                ResourceLocation id,
                FriendlyByteBuf.Reader<P> reader,
                BiConsumer<P, FriendlyByteBuf> writer,
                TriConsumer<P, MinecraftServer, ServerPlayer> handler
        );
    }
    public interface ClientboundRegistry {
        <P extends CustomPacketPayload> void register(
                ResourceLocation id,
                FriendlyByteBuf.Reader<P> reader,
                BiConsumer<P, FriendlyByteBuf> writer,
                Consumer<P> handler
        );
    }

    // ---- Packets ---- \\

    public static void loadServerbound(ServerboundRegistry registry) {
        registry.register(ServerboundRadioUpdatePacket.ID, ServerboundRadioUpdatePacket::read, ServerboundRadioUpdatePacket::write, SimpleRadioNetworking::handleRadioUpdate);
        registry.register(ServerboundRequestRouterPacket.ID, ServerboundRequestRouterPacket::read, ServerboundRequestRouterPacket::write, SimpleRadioNetworking::handleRequestRouter);
    }

    public static void loadClientbound(ClientboundRegistry registry) {
        registry.register(ClientboundActivityPacket.ID, ClientboundActivityPacket::read, ClientboundActivityPacket::write, SimpleRadioClientNetworking::handleActivityPacket);
        registry.register(ClientboundRegisterRouterPacket.ID, ClientboundRegisterRouterPacket::read, ClientboundRegisterRouterPacket::write, SimpleRadioClientNetworking::handleRegisterRouter);
        registry.register(ClientboundSpeakSoundPacket.ID, ClientboundSpeakSoundPacket::read, ClientboundSpeakSoundPacket::write, SimpleRadioClientNetworking::handleSpeakSound);
        registry.register(ClientboundWireEffectPacket.ID, ClientboundWireEffectPacket::read, ClientboundWireEffectPacket::write, SimpleRadioClientNetworking::handleWireEffect);
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
