package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.ItemHolder;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.core.networking.SimpleRadioNetworking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.SimpleRadioParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class FabricLoader {
    public static void loadItems() {
        SimpleRadioItems.ITEMS.forEach(((location, item) -> {
            Registry.register(BuiltInRegistries.ITEM, location, item.get());
        }));
    }

    public static void loadBlocks() {
        SimpleRadioBlocks.BLOCKS.forEach(((location, block) -> Registry.register(BuiltInRegistries.BLOCK, location, block)));
    }

    public static void loadParticles() {
        SimpleRadioParticles.PARTICLES.forEach(((location, particleType) -> Registry.register(BuiltInRegistries.PARTICLE_TYPE, location, particleType)));
    }

    public static void loadComponents() {
        SimpleRadioComponents.COMPONENT_TYPES.forEach(((location, componentType) -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, location, componentType)));
    }

    public static void loadPackets() {
        SimpleRadioNetworking.loadServerbound(new SimpleRadioNetworking.ServerboundRegistry() {
            @Override
            public <P extends CustomPacket> void register(CustomPacketPayload.Type<P> type, Class<P> packetClass, StreamCodec<RegistryFriendlyByteBuf, P> codec, TriConsumer<P, MinecraftServer, ServerPlayer> handler) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, serverbound(handler));
            }
        });

        SimpleRadioNetworking.loadClientbound(new SimpleRadioNetworking.ClientboundRegistry() {
            @Override
            public <P extends CustomPacket> void register(CustomPacketPayload.Type<P> type, Class<P> packetClass, StreamCodec<RegistryFriendlyByteBuf, P> codec, Consumer<P> handler) {
                PayloadTypeRegistry.playS2C().register(type, codec);
            }
        });
    }

    public static void loadClientPackets() {
        SimpleRadioNetworking.loadClientbound(new SimpleRadioNetworking.ClientboundRegistry() {
            @Override
            public <P extends CustomPacket> void register(CustomPacketPayload.Type<P> type, Class<P> packetClass, StreamCodec<RegistryFriendlyByteBuf, P> codec, Consumer<P> handler) {
                ClientPlayNetworking.registerGlobalReceiver(type, clientbound(handler));
            }
        });
    }

    public static <P extends CustomPacketPayload> ServerPlayNetworking.PlayPayloadHandler<P> serverbound(TriConsumer<P, MinecraftServer, ServerPlayer> consumer) {
        return (payload, context) -> consumer.accept(payload, context.server(), context.player());
    }
    public static <P extends CustomPacketPayload> ClientPlayNetworking.PlayPayloadHandler<P> clientbound(Consumer<P> consumer) {
        return (payload, context) -> consumer.accept(payload);
    }

    // -------- Resource Conditions -------- \\

    public static final ResourceConditionType<ItemsEnabledCondition> ITEMS_ENABLED =
            ResourceConditionType.create(CommonSimpleRadio.id("items_enabled"), ItemsEnabledCondition.CODEC);

    public record ItemsEnabledCondition(List<String> items) implements ResourceCondition {
        public static final MapCodec<ItemsEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("values").forGetter(ItemsEnabledCondition::items)
        ).apply(instance, ItemsEnabledCondition::new));

        @Override
        public ResourceConditionType<?> getType() {
            return ITEMS_ENABLED;
        }

        @Override
        public boolean test(@Nullable HolderLookup.Provider registryLookup) {

            for (String itemName : items) {
                ItemHolder<Item> holder = SimpleRadioItems.getByName(itemName);
                if (holder != null) return holder.enabled;
            }

            return true;
        }
    }

    public static ResourceCondition itemsEnabled(String... items) {
        return new ItemsEnabledCondition(List.of(items));
    }

    public static void load() {
        loadItems();
        loadBlocks();
        loadPackets();
        loadParticles();
        loadComponents();

        CommonSimpleRadio.load();

        ResourceConditions.register(ITEMS_ENABLED);
    }
}
