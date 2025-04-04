package com.codinglitch.simpleradio.client.core.central;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import com.mojang.blaze3d.audio.OggAudioStream;
import org.joml.Math;

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
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        short[] data = new short[shortBuffer.limit()];

        shortBuffer.get(data);
        shortBuffer.flip();
        shortBuffer.put(0, effect.apply(data));
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
