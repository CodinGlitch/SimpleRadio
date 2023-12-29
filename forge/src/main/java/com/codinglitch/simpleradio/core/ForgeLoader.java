package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundRadioPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeLoader {
    public static final SimpleChannel CHANNEL = ChannelBuilder.named(new ResourceLocation(CommonSimpleRadio.ID,"channel"))
            .optional()
            .networkProtocolVersion(0)
            .simpleChannel();

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> SimpleRadioItems.ITEMS.forEach((helper::register)));
    }

    public static void loadPackets() {
        CHANNEL.messageBuilder(ClientboundRadioPacket.class).decoder(ClientboundRadioPacket::decode).encoder(ClientboundRadioPacket::encode)
                .consumerMainThread(clientbound(ClientboundRadioPacket::handle)).add();
    }

    public static <P> BiConsumer<P, CustomPayloadEvent.Context> serverbound(Consumer<P> consumer) {
        return (packet, context) -> {
            consumer.accept(packet);
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