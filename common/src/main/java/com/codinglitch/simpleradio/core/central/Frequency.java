package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.RadioChannel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

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
    private static final int FREQUENCY_WHOLE_PLACES = 3;
    private static final int FREQUENCY_DECIMAL_PLACES = 2;
    private static final int FREQUENCY_DIGITS = FREQUENCY_WHOLE_PLACES+FREQUENCY_DECIMAL_PLACES;
    private static final int MAX_FREQUENCY = (int) java.lang.Math.pow(10, FREQUENCY_DIGITS);
    private static final String FREQUENCY_PATTERN = "^\\d{"+FREQUENCY_WHOLE_PLACES+"}.\\d{"+FREQUENCY_DECIMAL_PLACES+"}$";

    public final Modulation modulation;
    public final String frequency;
    public final List<RadioChannel> listeners;

    public Frequency(String frequency, Modulation modulation) {
        if (!validate(frequency))
            throw new IllegalArgumentException(frequency + " does not follow frequency pattern!");

        this.frequency = frequency;
        this.modulation = modulation;
        this.listeners = new ArrayList<>();

        frequencies.add(this);
    }

    @Nullable
    public static Modulation modulationOf(String shorthand) {
        for (Modulation modulation : Modulation.values())
            if (modulation.shorthand.equals(shorthand)) return modulation;
        return null;
    }

    public static boolean validate(String frequency) {
        return frequency.matches(FREQUENCY_PATTERN);
    }

    public static String incrementFrequency(String frequency, int amount) {
        int rawFrequency = Integer.parseInt(frequency.replaceAll("[.]", ""));
        String str = String.format("%0"+FREQUENCY_DIGITS+"d", Math.clamp(rawFrequency + amount, 0, MAX_FREQUENCY-1));
        return new StringBuilder(str).insert(str.length() - FREQUENCY_DECIMAL_PLACES, ".").toString();
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
