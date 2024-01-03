package com.codinglitch.simpleradio.radio;

import net.minecraft.world.entity.player.Player;

import java.util.*;

public class Frequency {
    private static final List<Frequency> frequencies = new ArrayList<>();

    //TODO: move this to a config
    private static final String FREQUENCY_PATTERN = "^\\d{3}.\\d{2}$";

    public final String frequency;
    public final List<RadioChannel> listeners;

    public Frequency(String frequency) {
        if (!frequency.matches(FREQUENCY_PATTERN))
            throw new IllegalArgumentException(frequency + " does not follow frequency pattern!");

        this.frequency = frequency;
        this.listeners = new ArrayList<>();

        frequencies.add(this);
    }

    public static int getFrequency(String string) {
        for (int i = 0; i < frequencies.size(); i++) {
            Frequency frequency = frequencies.get(i);
            if (frequency.frequency.equals(string))
                return i;
        }

        return -1;
    }

    public void addListener(Player player) {
        RadioChannel channel = new RadioChannel(player);
        listeners.add(channel);
    }

    public void removeListener(Player player) {
        removeListener(player.getUUID());
    }
    public void removeListener(UUID player) {
        listeners.removeIf(channel -> channel.owner.equals(player));
    }

    public static Frequency getOrCreateFrequency(String frequency) {
        int index = getFrequency(frequency);
        if (index != -1) return frequencies.get(index);

        return new Frequency(frequency);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return obj.equals(this.frequency);
        } else {
            return super.equals(this);
        }
    }
}
