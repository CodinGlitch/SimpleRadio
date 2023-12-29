package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.core.FabricLoader;
import net.fabricmc.api.ClientModInitializer;

public class SimpleRadioClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader.loadClientPackets();
    }
}
