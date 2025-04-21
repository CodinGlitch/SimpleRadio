package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.ForgeLoader;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.PacketDistributor;

public class ForgeClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(CustomPacketPayload packet) {
        ForgeLoader.CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }
}
