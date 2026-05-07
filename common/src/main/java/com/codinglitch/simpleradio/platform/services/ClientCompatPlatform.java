package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.joml.Quaternionf;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public interface ClientCompatPlatform {
    default CompletableFuture<AudioStream> makeSubstream(SoundInstance sound) {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        SoundEngine soundEngine = soundManager.soundEngine;

        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream inputStream = soundEngine.soundBuffers.resourceManager.open(sound.getSound().getPath());
                return new JOrbisAudioStream(inputStream);
            } catch (IOException var4) {
                throw new CompletionException(var4);
            }
        }, Util.backgroundExecutor());
    }

    SoundInstance makeSound(Router router, String soundString, float volume, float pitch, float severity, float offset, long seed);

    WorldlyPosition modifyPosition(WorldlyPosition position);

    Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation);
}