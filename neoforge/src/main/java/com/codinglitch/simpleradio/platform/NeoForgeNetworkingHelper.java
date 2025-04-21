package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class NeoForgeNetworkingHelper implements NetworkingHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        player.connection.send(packet);
    }
}