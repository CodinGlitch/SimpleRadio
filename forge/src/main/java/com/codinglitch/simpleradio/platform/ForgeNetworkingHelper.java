package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.ForgeLoader;
import com.codinglitch.simpleradio.core.networking.Packeter;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import com.codinglitch.simpleradio.platform.services.PlatformHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;

public class ForgeNetworkingHelper implements NetworkingHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, Packeter packet) {
        ForgeLoader.CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }
}