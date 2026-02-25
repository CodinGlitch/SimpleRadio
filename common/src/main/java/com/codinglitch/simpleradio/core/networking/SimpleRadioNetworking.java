package com.codinglitch.simpleradio.core.networking;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.core.SimpleRadioClientNetworking;
import com.codinglitch.simpleradio.core.networking.packets.*;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.UUID;
import java.util.function.Consumer;

public class SimpleRadioNetworking {
    public interface ServerboundRegistry {
        <P extends CustomPacket> void register(
                CustomPacketPayload.Type<P> type, Class<P> packetClass,
                StreamCodec<RegistryFriendlyByteBuf, P> codec,
                TriConsumer<P, MinecraftServer, ServerPlayer> handler
        );
    }
    public interface ClientboundRegistry {
        <P extends CustomPacket> void register(
                CustomPacketPayload.Type<P> type, Class<P> packetClass,
                StreamCodec<RegistryFriendlyByteBuf, P> codec,
                Consumer<P> handler
        );
    }

    // ---- Packets ---- \\

    public static void loadServerbound(ServerboundRegistry registry) {
        registry.register(ServerboundRadioUpdatePacket.TYPE, ServerboundRadioUpdatePacket.class, ServerboundRadioUpdatePacket.STREAM_CODEC, SimpleRadioNetworking::handleRadioUpdate);
        registry.register(ServerboundRequestRouterPacket.TYPE, ServerboundRequestRouterPacket.class, ServerboundRequestRouterPacket.STREAM_CODEC, SimpleRadioNetworking::handleRequestRouter);
    }

    public static void loadClientbound(ClientboundRegistry registry) {
        registry.register(ClientboundActivityPacket.TYPE, ClientboundActivityPacket.class, ClientboundActivityPacket.STREAM_CODEC, SimpleRadioClientNetworking::handleActivityPacket);
        registry.register(ClientboundRegisterRouterPacket.TYPE, ClientboundRegisterRouterPacket.class, ClientboundRegisterRouterPacket.STREAM_CODEC, SimpleRadioClientNetworking::handleRegisterRouter);
        registry.register(ClientboundSpeakSoundPacket.TYPE, ClientboundSpeakSoundPacket.class, ClientboundSpeakSoundPacket.STREAM_CODEC, SimpleRadioClientNetworking::handleSpeakSound);
        registry.register(ClientboundWireEffectPacket.TYPE, ClientboundWireEffectPacket.class, ClientboundWireEffectPacket.STREAM_CODEC, SimpleRadioClientNetworking::handleWireEffect);
    }

    // ---- Handlers ---- \\

    public static void handleRadioUpdate(ServerboundRadioUpdatePacket packet, MinecraftServer server, ServerPlayer player) {
        server.execute(() -> {
            if (!RadioManager.getInstance().frequencies().check(packet.frequency())) return;

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
        String type = packet.routerType();
        short mapping = packet.mapping();

        server.execute(() -> {
            short identifier = RadioManager.getInstance().getIdentifier(r -> reference.equals(r.getReference()) && r.getClass().getSimpleName().equals(type));
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
