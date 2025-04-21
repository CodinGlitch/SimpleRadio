package com.codinglitch.simpleradio.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ClientNetworkingHelper {
    void sendToServer(CustomPacketPayload packet);
}