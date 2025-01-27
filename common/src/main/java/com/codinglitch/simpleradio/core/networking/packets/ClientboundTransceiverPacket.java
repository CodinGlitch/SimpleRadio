package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.CommonSimpleRadioClient;
import com.codinglitch.simpleradio.core.central.Packeter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundTransceiverPacket(boolean started, UUID player, String type) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "transceiver_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.started);
        buffer.writeUUID(this.player);
        buffer.writeUtf(this.type);
    }

    public static ClientboundTransceiverPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundTransceiverPacket(buffer.readBoolean(), buffer.readUUID(), buffer.readUtf());
    }

    public static void handle(ClientboundTransceiverPacket packet) {
        boolean started = packet.started();
        UUID player = packet.player();

        Minecraft.getInstance().execute(() -> {
            CommonSimpleRadioClient.isTransmitting.put(player, started);
        });
    }
}