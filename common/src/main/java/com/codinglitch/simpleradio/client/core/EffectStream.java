package com.codinglitch.simpleradio.client.core;

import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.audio.SoundBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class EffectStream extends OggAudioStream {
    public AudioEffect effect;

    public EffectStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    public void applyEffect(ByteBuffer buffer, int size) {
        short[] data = new short[size/2];

        buffer.asShortBuffer().get(data);
        buffer.asShortBuffer().put(effect.apply(data));
    }

    public ByteBuffer push(int size) throws IOException {
        return super.read(size);
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        ByteBuffer buffer = super.read(size);
        this.applyEffect(buffer, size);

        return buffer;
    }

    @Override
    public ByteBuffer readAll() throws IOException {
        ByteBuffer buffer = super.readAll();
        this.applyEffect(buffer, buffer.limit());

        return buffer;
    }
}
