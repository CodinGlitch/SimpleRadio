package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.NeoforgeLoader;
import com.codinglitch.simpleradio.core.networking.Packeter;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeNetworkingHelper implements NetworkingHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, Packeter packet) {
        NeoforgeLoader.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}