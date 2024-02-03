package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundRadioPacket;
import com.codinglitch.simpleradio.core.networking.packets.ServerboundRadioUpdatePacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeLoader {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CommonSimpleRadio.ID,"channel"),
            () -> "0",
            "0"::equals,
            "0"::equals
    );

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> SimpleRadioItems.ITEMS.forEach((helper::register)));
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> SimpleRadioBlocks.BLOCKS.forEach((helper::register)));
        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> SimpleRadioBlockEntities.BLOCK_ENTITIES.forEach((helper::register)));
        event.register(ForgeRegistries.Keys.MENU_TYPES, helper -> SimpleRadioMenus.MENUS.forEach(helper::register));
        event.register(Registries.CREATIVE_MODE_TAB, helper -> SimpleRadioMenus.CREATIVE_TABS.forEach(helper::register));
    }

    public static void loadPackets() {
        int index = 0;

        CHANNEL.messageBuilder(ServerboundRadioUpdatePacket.class, index++).decoder(ServerboundRadioUpdatePacket::decode).encoder(ServerboundRadioUpdatePacket::encode)
                .consumerMainThread(serverbound(ServerboundRadioUpdatePacket::handle)).add();

        CHANNEL.messageBuilder(ClientboundRadioPacket.class, index++).decoder(ClientboundRadioPacket::decode).encoder(ClientboundRadioPacket::encode)
                .consumerMainThread(clientbound(ClientboundRadioPacket::handle)).add();
    }

    private static <P> BiConsumer<P, Supplier<NetworkEvent.Context>> serverbound(TriConsumer<P, MinecraftServer, ServerPlayer> consumer) {
        return (packet, supplier) -> {
            NetworkEvent.Context context = supplier.get();
            consumer.accept(packet, context.getSender().getServer(), context.getSender());
            context.setPacketHandled(true);
        };
    }
    public static <P> BiConsumer<P, Supplier<NetworkEvent.Context>> clientbound(Consumer<P> consumer) {
        return (packet, supplier) -> {
            NetworkEvent.Context context = supplier.get();
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
