package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class NeoForgeClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(CustomPacketPayload packet) {
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(packet);
        }
    }
}
