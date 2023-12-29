package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.networking.Packeter;
import net.minecraft.server.level.ServerPlayer;

public interface NetworkingHelper {
    void sendToPlayer(ServerPlayer player, Packeter packet);
}