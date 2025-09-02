package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.cc.SocketPeripheral;
import com.codinglitch.simpleradio.routers.Router;
import com.mojang.blaze3d.audio.OggAudioStream;
import dan200.computercraft.api.peripheral.IPeripheral;
import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import gg.moonflower.etched.api.sound.AbstractOnlineSoundInstance;
import gg.moonflower.etched.api.sound.SoundTracker;
import gg.moonflower.etched.api.sound.StopListeningSound;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.core.mixin.client.LevelRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class EtchedCompat {
    public static String getSound(ItemStack record) {
        TrackData[] tracks = PlayableRecord.getStackMusic(record).orElse(null);
        if (tracks == null) return null;

        return tracks[0].url();
    }

    public static CompletableFuture<AudioStream> makeSubstream(AbstractSoundInstance sound) {
        if (sound instanceof AbstractOnlineSoundInstance onlineSoundInstance) {
            Minecraft mc = Minecraft.getInstance();
            SoundManager soundManager = mc.getSoundManager();
            SoundEngine soundEngine = soundManager.soundEngine;

            return onlineSoundInstance.getStream(soundEngine.soundBuffers, sound.getSound(), false);
        }

        return null;
    }

    public static AbstractSoundInstance makeSound(Router router, String url, long seed) {
        WorldlyPosition location = router.getLocation();

        Minecraft mc = Minecraft.getInstance();
        BlockPos pos = location.blockPos();
        Map<BlockPos, SoundInstance> playingRecords = ((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getPlayingRecords();

        SoundInstance soundInstance = playingRecords.get(pos);
        if (soundInstance != null) {
            mc.getSoundManager().stop(soundInstance);
            playingRecords.remove(pos);
        }

        AbstractOnlineSoundInstance instance = SoundTracker.getEtchedRecord(
                url,
                Component.empty(),
                mc.level,
                pos,
                AudioSource.AudioFileType.FILE
        );
        instance.resolve(mc.getSoundManager());

        playingRecords.put(pos, instance);

        return instance;
    }
}
