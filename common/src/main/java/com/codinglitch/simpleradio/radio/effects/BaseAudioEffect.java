package com.codinglitch.simpleradio.radio.effects;

import java.util.Random;

public class BaseAudioEffect extends AudioEffect {
    public static Random RANDOM = new Random();

    @Override
    public short[] apply(short[] data) {
        for (int i = 0; i < data.length; i++) {
            if (RANDOM.nextFloat(100) < severity) data[i] *= 0;
        }
        return data;
    }
}
