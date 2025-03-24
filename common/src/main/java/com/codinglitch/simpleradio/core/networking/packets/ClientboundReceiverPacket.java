package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundReceiverPacket(int time, UUID uuid) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "radio_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.time);
        buffer.writeUUID(this.uuid);
    }

    public static ClientboundReceiverPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundReceiverPacket(buffer.readInt(), buffer.readUUID());
    }

    public static void handle(ClientboundReceiverPacket packet) {
        int time = packet.time();
        UUID uuid = packet.uuid();

        Minecraft.getInstance().execute(() -> {
            RadioReceiver receiver = ClientRadioManager.getReceiver(uuid);
            if (receiver == null) return;

            receiver.receivingTime = time;
        });
    }
}