package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class FabricClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(CustomPacket packet) {
        ClientPlayNetworking.send(packet.id(), packet.writeNew());
    }
}
