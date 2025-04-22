package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import net.minecraft.server.level.ServerPlayer;

public class NeoForgeNetworkingHelper implements NetworkingHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacket packet) {
        player.connection.send(packet);
    }
}