package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.ItemHolder;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.core.networking.SimpleRadioNetworking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioComponents;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.SimpleRadioParticles;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
    }

    public static void loadClientPackets() {
        SimpleRadioNetworking.loadClientbound(new SimpleRadioNetworking.ClientboundRegistry() {
            @Override
            public <P extends CustomPacket> void register(CustomPacketPayload.Type<P> type, Class<P> packetClass, StreamCodec<RegistryFriendlyByteBuf, P> codec, Consumer<P> handler) {
                PayloadTypeRegistry.playS2C().register(type, codec);
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

    private static final ResourceLocation ITEMS_ENABLED = CommonSimpleRadio.id("items_enabled");

    public static ConditionJsonProvider itemsEnabled(String... items) {
        return new ConditionJsonProvider() {
            @Override
            public void writeParameters(JsonObject object) {
                JsonArray array = new JsonArray();
                for (var item : items) {
                    array.add(item);
                }
                object.add("values", array);
            }

            @Override
            public ResourceLocation getConditionId() {
                return ITEMS_ENABLED;
            }
        };
    }

    static {
        ResourceConditions.register(ITEMS_ENABLED, object -> {
            JsonArray array = GsonHelper.getAsJsonArray(object, "values");

            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    ItemHolder<Item> holder = SimpleRadioItems.getByName(element.getAsString());
                    if (holder != null) return holder.enabled;
                } else {
                    throw new JsonParseException("Invalid item entry: " + element);
                }
            }

            return true;
        });
    }

    public static void load() {
        loadItems();
        loadBlocks();
        loadPackets();
        loadParticles();
        loadComponents();

        CommonSimpleRadio.load();
    }
}
