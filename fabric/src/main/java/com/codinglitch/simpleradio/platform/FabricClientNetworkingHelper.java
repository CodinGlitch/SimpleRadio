package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(CustomPacket packet) {
        ClientPlayNetworking.send(packet);
    }
}
