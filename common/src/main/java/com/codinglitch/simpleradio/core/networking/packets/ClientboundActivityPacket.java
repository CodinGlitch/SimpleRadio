package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ClientboundActivityPacket(float activity, short identifier) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "activity");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.activity);
        buffer.writeShort(this.identifier);
    }

    public static ClientboundActivityPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundActivityPacket(buffer.readShort(), buffer.readShort());
    }

    public static void handle(ClientboundActivityPacket packet) {
        float activity = packet.activity();
        short identifier = packet.identifier();

        Minecraft.getInstance().execute(() -> {
            RadioRouter router = ClientRadioManager.getRouter(identifier);
            if (router == null) return;

            router.activity = activity;
            router.activityTime = 20;
        });
    }
}