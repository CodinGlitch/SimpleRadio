package com.codinglitch.simpleradio.compat.etched;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.routers.Router;
import gg.moonflower.etched.api.sound.AbstractOnlineSoundInstance;
import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.api.sound.StopListeningSound;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.core.mixin.client.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClientEtchedCompat {
    public static CompletableFuture<AudioStream> makeSubstream(SoundInstance sound) {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        SoundEngine soundEngine = soundManager.soundEngine;

        if (sound instanceof AbstractOnlineSoundInstance onlineSoundInstance) {
            return onlineSoundInstance.getStream(soundEngine.soundBuffers, sound.getSound(), false);
        } else if (sound instanceof StopListeningSound stopListeningSound) {
            return stopListeningSound.getStream(soundEngine.soundBuffers, sound.getSound(), false);
        }

        return null;
    }

    public static SoundInstance makeSound(Router router, String data, float volume, float pitch, float severity, float offset, long seed) {
        WorldlyPosition location = router.getLocation();

        int index = 0;
        List<String> parts = Arrays.asList(data.split("\\|"));

        if (parts.size() > 1) {
            index = Integer.parseInt(parts.get(0));
            data = parts.get(index+1);
        }

        Minecraft mc = Minecraft.getInstance();
        BlockPos pos = location.blockPos();
        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getPlayingRecords();

        SoundInstance soundInstance = playingRecords.get(pos);
        if (soundInstance != null) {
            mc.getSoundManager().stop(soundInstance);
            playingRecords.remove(pos);
        }

        SoundInstance instance = SoundTracker.getEtchedRecord(
                data,
                Component.empty(),
                mc.level,
                pos,
                AudioSource.AudioFileType.FILE
        );

        if (parts.size() > 1) {
            parts.set(0, String.valueOf( (index+1) % (parts.size()-1) ));

            instance = StopListeningSound.create(instance, () -> mc.tell(() -> {
                ClientRadioManager.speakSound(router.getReference(), String.join("|", parts), volume, pitch, severity, offset, seed);
            }));
        }

        instance.resolve(mc.getSoundManager());
        playingRecords.put(pos, instance);

        return instance;
    }
}
