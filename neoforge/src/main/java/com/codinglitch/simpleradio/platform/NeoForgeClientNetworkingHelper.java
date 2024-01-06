package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.NeoforgeLoader;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.platform.services.ClientNetworkingHelper;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeClientNetworkingHelper implements ClientNetworkingHelper {
    @Override
    public void sendToServer(Packeter packet) {
        NeoforgeLoader.CHANNEL.send(PacketDistributor.SERVER.noArg(), packet);
    }
}
