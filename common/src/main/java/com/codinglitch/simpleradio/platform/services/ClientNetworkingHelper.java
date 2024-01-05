package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.networking.Packeter;
import net.minecraft.server.level.ServerPlayer;

public interface ClientNetworkingHelper {
    void sendToServer(Packeter packet);
}