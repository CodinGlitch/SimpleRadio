package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.networking.CustomPacket;

public interface ClientNetworkingHelper {
    void sendToServer(CustomPacket packet);
}