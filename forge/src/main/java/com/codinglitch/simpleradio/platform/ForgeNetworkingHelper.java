package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.ForgeLoader;
import com.codinglitch.simpleradio.core.networking.CustomPacket;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ForgeNetworkingHelper implements NetworkingHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacket packet) {
        ForgeLoader.CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }
}