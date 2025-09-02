package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.compat.EtchedCompat;
import com.codinglitch.simpleradio.platform.services.ClientCompatPlatform;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.sounds.AudioStream;

import java.util.concurrent.CompletableFuture;

public class ForgeClientCompatPlatform implements ClientCompatPlatform {
    @Override
    public CompletableFuture<AudioStream> makeSubstream(AbstractSoundInstance sound) {
        // ---- Etched ---- \\
        if (CompatCore.ETCHED.enabled) {
            CompletableFuture<AudioStream> result = EtchedCompat.makeSubstream(sound);
            if (result != null) return result;
        }

        return ClientCompatPlatform.super.makeSubstream(sound);
    }

    @Override
    public AbstractSoundInstance makeSound(Router router, String soundString, long seed) {
        // ---- Etched ---- \\
        if (CompatCore.ETCHED.enabled) {
            AbstractSoundInstance result = EtchedCompat.makeSound(router, soundString, seed);
            if (result != null) return result;
        }

        return null;
    }
}