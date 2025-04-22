package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkingHelper implements NetworkingHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacket packet) {
        ServerPlayNetworking.send(player, packet.id(), packet.writeNew());
    }
}
