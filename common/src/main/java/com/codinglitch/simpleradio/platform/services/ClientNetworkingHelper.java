package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.central.Packeter;

public interface ClientNetworkingHelper {
    void sendToServer(Packeter packet);
}