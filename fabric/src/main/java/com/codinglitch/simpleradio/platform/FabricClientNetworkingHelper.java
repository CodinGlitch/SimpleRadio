package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(Packeter packet) {
        ClientPlayNetworking.send(packet.resource(), packet.toBuffer());
    }
}
