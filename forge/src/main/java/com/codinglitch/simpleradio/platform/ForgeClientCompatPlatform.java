package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.compat.etched.ClientEtchedCompat;
import com.codinglitch.simpleradio.compat.etched.EtchedCompat;
import com.codinglitch.simpleradio.platform.services.ClientCompatPlatform;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;

import java.util.concurrent.CompletableFuture;

public class ForgeClientCompatPlatform implements ClientCompatPlatform {
    @Override
    public CompletableFuture<AudioStream> makeSubstream(SoundInstance sound) {
        // ---- Etched ---- \\
        if (CompatCore.ETCHED.enabled) {
            CompletableFuture<AudioStream> result = ClientEtchedCompat.makeSubstream(sound);
            if (result != null) return result;
        }

        return ClientCompatPlatform.super.makeSubstream(sound);
    }

    @Override
    public SoundInstance makeSound(Router router, String soundString, float volume, float pitch, float severity, float offset, long seed) {
        // ---- Etched ---- \\
        if (CompatCore.ETCHED.enabled) {
            SoundInstance result = ClientEtchedCompat.makeSound(router, soundString, volume, pitch, severity, offset, seed);
            if (result != null) return result;
        }

        return null;
    }
}