package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Catalyst;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.core.networking.SimpleRadioNetworking;
import com.google.common.collect.Maps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record ClientboundCatalystSyncPacket(Map<ResourceLocation, Catalyst> catalysts) implements CustomPacket {
    public static Type<ClientboundCatalystSyncPacket> TYPE = new Type<>(CommonSimpleRadio.id("catalyst_sync"));
    public static StreamCodec<RegistryFriendlyByteBuf, ClientboundCatalystSyncPacket> STREAM_CODEC = StreamCodec.ofMember(
            ClientboundCatalystSyncPacket::write, ClientboundCatalystSyncPacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(catalysts.size());
        catalysts.forEach((location, catalyst) -> {
            buffer.writeResourceLocation(location);
            SimpleRadioNetworking.writeCatalyst(buffer, catalyst);
        });
    }

    public static ClientboundCatalystSyncPacket read(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Map<ResourceLocation, Catalyst> data = Maps.newHashMapWithExpectedSize(count);

        for (int i = 0; i < count; i++) {
            ResourceLocation location = buffer.readResourceLocation();
            Catalyst catalyst = SimpleRadioNetworking.readCatalyst(buffer, location);
            data.put(location, catalyst);
        }

        return new ClientboundCatalystSyncPacket(data);
    }
}