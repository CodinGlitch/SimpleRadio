package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundRegisterRouterPacket(short mapping, short identifier) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "register_router");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeShort(this.mapping);
        buffer.writeShort(this.identifier);
    }

    public static ClientboundRegisterRouterPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundRegisterRouterPacket(buffer.readShort(), buffer.readShort());
    }

    public static void handle(ClientboundRegisterRouterPacket packet) {
        short mapping = packet.mapping();
        short identifier = packet.identifier();

        Minecraft.getInstance().execute(() -> ClientRadioManager.finalizeRouter(mapping, identifier));
    }
}