package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundRadioPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.MessageFunctions;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NeoforgeLoader {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CommonSimpleRadio.ID, "main"), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );


    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> SimpleRadioItems.ITEMS.forEach((helper::register)));
    }

    public static void loadItems() {

    }

    public static void loadPackets() {
        int incrementer = 1;

        CHANNEL.registerMessage(incrementer++, ClientboundRadioPacket.class, ClientboundRadioPacket::encode, ClientboundRadioPacket::decode,
                clientbound(ClientboundRadioPacket::handle));
    }

    public static <P> MessageFunctions.MessageConsumer<P> serverbound(Consumer<P> consumer) {
        return (packet, context) -> {
            consumer.accept(packet);
            context.setPacketHandled(true);
        };
    }
    public static <P> MessageFunctions.MessageConsumer<P> clientbound(Consumer<P> consumer) {
        return (packet, context) -> {
            consumer.accept(packet);
            context.setPacketHandled(true);
        };
    }

    public static void load() {
        loadItems();
        loadPackets();
    }
}