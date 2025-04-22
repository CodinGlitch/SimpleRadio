package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import net.minecraft.client.Minecraft;

public class NeoForgeClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(CustomPacket packet) {
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(packet);
        }
    }
}
