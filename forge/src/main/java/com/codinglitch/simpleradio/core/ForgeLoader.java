package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.CatalystRegistry;
import com.codinglitch.simpleradio.api.FrequencingRegistry;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundSpeakSoundPacket;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundTransceiverPacket;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundWireEffectPacket;
import com.codinglitch.simpleradio.core.networking.packets.ServerboundRadioUpdatePacket;
import com.codinglitch.simpleradio.core.registry.*;
import com.codinglitch.simpleradio.datagen.SimpleRadioBlockLootTableProvider;
import com.codinglitch.simpleradio.datagen.SimpleRadioRecipeProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = CommonSimpleRadio.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeLoader {
    private static final String PROTOCOL_VERSION = "0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CommonSimpleRadio.ID,"channel"),
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

        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, helper -> {
            CraftingHelper.register(ItemsEnabledCondition.Serializer.INSTANCE);
        });

        SimpleRadioCatalysts.load();
        SimpleRadioFrequencing.load();
    }

    public static void loadPackets() {
        int index = 0;

        CHANNEL.messageBuilder(ServerboundRadioUpdatePacket.class, index++).decoder(ServerboundRadioUpdatePacket::decode).encoder(ServerboundRadioUpdatePacket::encode)
                .consumerMainThread(serverbound(ServerboundRadioUpdatePacket::handle)).add();

        CHANNEL.messageBuilder(ClientboundTransceiverPacket.class, index++).decoder(ClientboundTransceiverPacket::decode).encoder(ClientboundTransceiverPacket::encode)
                .consumerMainThread(clientbound(ClientboundTransceiverPacket::handle)).add();
        CHANNEL.messageBuilder(ClientboundWireEffectPacket.class, index++).decoder(ClientboundWireEffectPacket::decode).encoder(ClientboundWireEffectPacket::encode)
                .consumerMainThread(clientbound(ClientboundWireEffectPacket::handle)).add();

        CHANNEL.messageBuilder(ClientboundSpeakSoundPacket.class, index++).decoder(ClientboundSpeakSoundPacket::decode).encoder(ClientboundSpeakSoundPacket::encode)
                .consumerMainThread(clientbound(ClientboundSpeakSoundPacket::handle)).add();
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
