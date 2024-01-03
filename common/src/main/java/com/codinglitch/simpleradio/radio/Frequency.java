package com.codinglitch.simpleradio.radio;

import net.minecraft.world.entity.player.Player;

import java.util.*;

public class Frequency {
    public enum Modulation {
        FREQUENCY("FM"),
        AMPLITUDE("AM");

        public final String shorthand;

        Modulation(String shorthand) {
            this.shorthand = shorthand;
        }
    }

    private static final List<Frequency> frequencies = new ArrayList<>();

    //TODO: move this to a config
    private static final String FREQUENCY_PATTERN = "^\\d{3}.\\d{2}$";

    public final Modulation modulation;
    public final String frequency;
    public final List<RadioChannel> listeners;

    public Frequency(String frequency, Modulation modulation) {
        if (!frequency.matches(FREQUENCY_PATTERN))
            throw new IllegalArgumentException(frequency + " does not follow frequency pattern!");

        this.frequency = frequency;
        this.modulation = modulation;
        this.listeners = new ArrayList<>();

        frequencies.add(this);
    }

    public static Modulation modulationOf(String shorthand) {
        return Modulation.valueOf(shorthand);
    }

    public static int getFrequency(String string, Modulation modulation) {
        for (int i = 0; i < frequencies.size(); i++) {
            Frequency frequency = frequencies.get(i);
            if (frequency.frequency.equals(string) && frequency.modulation.equals(modulation))
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

        if (listeners.size() == 0)
            frequencies.remove(this);
    }

    public static Frequency getOrCreateFrequency(String frequency, Modulation modulation) {
        int index = getFrequency(frequency, modulation);
        if (index != -1) return frequencies.get(index);

        return new Frequency(frequency, modulation);
    }
}
