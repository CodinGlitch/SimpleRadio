package com.codinglitch.simpleradio.radio;

import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RadioSource implements Source {

    private static final Map<UUID, OpusDecoder> decoders = new ConcurrentHashMap<>();

    public byte[] data;
    public short[] decoded;

    public String sound;

    public RadioSource(byte[] data) {
        this.data = data;
    }

    public RadioSource(short[] pcm) {
        this.decoded = pcm;
    }

    public RadioSource(String sound) {
        this.sound = sound;
    }

    public static OpusDecoder getDecoder(UUID id) {
        return decoders.computeIfAbsent(id, uuid -> CommonRadioPlugin.serverApi.createDecoder());
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public short[] getDecoded(UUID id) {
        if (decoded == null) decoded = getDecoder(id).decode(data);
        return decoded;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse(this.sound));
    }

    @Override
    public String getSound() {
        return sound;
    }
}
