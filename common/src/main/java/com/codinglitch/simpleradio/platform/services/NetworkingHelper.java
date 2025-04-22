package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.networking.CustomPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface NetworkingHelper {
    void sendToPlayer(ServerPlayer player, CustomPacket packet);
}