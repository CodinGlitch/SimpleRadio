package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkingHelper implements NetworkingHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, Packeter packet) {
        ServerPlayNetworking.send(player, packet.resource(), packet.toBuffer());
    }
}
