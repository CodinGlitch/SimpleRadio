package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.central.Packeter;
import net.minecraft.server.level.ServerPlayer;

public interface NetworkingHelper {
    void sendToPlayer(ServerPlayer player, Packeter packet);
}