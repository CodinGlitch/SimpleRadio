package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.platform.services.ClientCompatPlatform;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.sounds.AudioStream;

import java.util.concurrent.CompletableFuture;

public class FabricClientCompatPlatform implements ClientCompatPlatform {
    @Override
    public CompletableFuture<AudioStream> makeSubstream(AbstractSoundInstance sound) {
        return ClientCompatPlatform.super.makeSubstream(sound);
    }

    @Override
    public AbstractSoundInstance makeSound(Router router, String soundString, long seed) {
        return null;
    }
}
