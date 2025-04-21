package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FabricClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(CustomPacketPayload packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buffer);

        ClientPlayNetworking.send(packet.id(), buffer);
    }
}
