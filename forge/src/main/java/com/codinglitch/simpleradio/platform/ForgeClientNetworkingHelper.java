package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.ForgeLoader;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import net.minecraftforge.network.PacketDistributor;

public class ForgeClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(Packeter packet) {
        ForgeLoader.CHANNEL.send(PacketDistributor.SERVER.noArg(), packet);
    }
}
