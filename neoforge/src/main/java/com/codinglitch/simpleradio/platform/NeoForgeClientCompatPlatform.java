package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.platform.services.ClientCompatPlatform;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import org.joml.Quaternionf;

import java.util.concurrent.CompletableFuture;

public class NeoForgeClientCompatPlatform implements ClientCompatPlatform {
    @Override
    public CompletableFuture<AudioStream> makeSubstream(SoundInstance sound) {
        return ClientCompatPlatform.super.makeSubstream(sound);
    }

    @Override
    public SoundInstance makeSound(Router router, String soundString, float volume, float pitch, float severity, float offset, long seed) {
        return null;
    }

    @Override
    public WorldlyPosition modifyPosition(WorldlyPosition position) {

        // ---- Valkyrien Skies ---- \\
        if (CompatCore.VALKYRIEN_SKIES.enabled) {
        }

        return position;
    }

    @Override
    public Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {

        // ---- Valkyrien Skies ---- \\
        if (CompatCore.VALKYRIEN_SKIES.enabled) {
        }

        return rotation;
    }
}
