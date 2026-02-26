package com.codinglitch.simpleradio.client.core.central;

import com.codinglitch.simpleradio.radio.effects.AudioEffect;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class EffectStream implements AudioStream {
    private final AudioStream substream;
    public AudioEffect effect;

    public EffectStream(InputStream inputStream) throws IOException {
        this(new JOrbisAudioStream(inputStream));
    }
    public EffectStream(AudioStream substream) {
        this.substream = substream;
    }

    public void applyEffect(ByteBuffer buffer, int size) {
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        short[] data = new short[shortBuffer.limit()];

        shortBuffer.get(data);
        shortBuffer.flip();
        shortBuffer.put(0, effect.apply(data));
    }

    public ByteBuffer push(int size) throws IOException {
        return this.substream.read(size);
    }

    @Override
    public AudioFormat getFormat() {
        return this.substream.getFormat();
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        ByteBuffer buffer = this.substream.read(size);
        this.applyEffect(buffer, 1);

        return buffer;
    }


    public ByteBuffer readAll() throws IOException {
        ByteBuffer buffer = null;
        if (this.substream instanceof JOrbisAudioStream oggStream) {
            buffer = oggStream.readAll();
        }
        if (buffer == null) return null;

        this.applyEffect(buffer, buffer.limit());
        return buffer;
    }

    @Override
    public void close() throws IOException {
        this.substream.close();
    }
}
