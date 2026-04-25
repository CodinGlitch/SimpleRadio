package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.core.central.FrequencyChannel;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class FrequenciesImpl implements Frequencies {
    // negative for AM, positive for FM; this will need to be changed if we add PM
    private final Map<Integer, Frequency> FREQUENCIES = new HashMap<>();

    public static String DEFAULT_FREQUENCY = "000.00FM";
    public static Frequency.Modulation DEFAULT_MODULATION = Frequency.Modulation.FREQUENCY;
    public static int FREQUENCY_DIGITS = 5;
    public static int MAX_FREQUENCY = 100000;
    public static String FREQUENCY_PATTERN = "auto-generate";

    public void close() {
        FREQUENCIES.clear();
    }

    @Override
    public String defaultFrequency() {
        return DEFAULT_FREQUENCY;
    }

    @Override
    public Frequency.Modulation defaultModulation() {
        return DEFAULT_MODULATION;
    }

    @Override
    public List<Frequency> get() {
        return FREQUENCIES.values().stream().toList();
    }

    @Override
    public Frequency get(String frequency, Frequency.Modulation modulation) {
        return FREQUENCIES.get(getFrequencyIndex(frequency, modulation));
    }

    @Override
    public Frequency getOrCreate(String frequency, Frequency.Modulation modulation) {
        if (frequency.isEmpty()) frequency = DEFAULT_FREQUENCY;
        if (modulation == null) modulation = DEFAULT_MODULATION;

        Frequency found = get(frequency, modulation);
        if (found != null) return found;

        return new FrequencyChannel(this, frequency, modulation);
    }

    @Override
    public Frequency tryParse(String string) {
        Frequency.Modulation modulation = modulationOf(string.substring(string.length() - 2));
        return getOrCreate(string.substring(0, string.length() - 2), modulation);
    }

    @Override
    public void add(Frequency frequency) {
        FREQUENCIES.put(frequency.getIndex(), frequency);
    }

    @Override
    public void remove(Frequency frequency) {
        FREQUENCIES.remove(frequency.getIndex());
    }

    @Override
    @Nullable
    public Frequency.Modulation modulationOf(String shorthand) {
        for (Frequency.Modulation modulation : Frequency.Modulation.values())
            if (modulation.shorthand.equals(shorthand)) return modulation;
        return null;
    }

    @Override
    public boolean check(String frequency) {
        return frequency.matches(FREQUENCY_PATTERN);
    }

    @Override
    public String incrementFrequency(String frequency, int amount) {
        int rawFrequency = Integer.parseInt(frequency.replaceAll("[.]", ""));
        String str = String.format("%0"+FREQUENCY_DIGITS+"d", Math.clamp(0, MAX_FREQUENCY-1, rawFrequency + amount));
        return new StringBuilder(str).insert(str.length() - SimpleRadioLibrary.SERVER_CONFIG.frequency.decimalPlaces, ".").toString();
    }

    @Override
    public List<Pair<Integer, Frequency>> getWithin(String frequency, Frequency.Modulation modulation, int distance) {
        int frequencyIndex = getFrequencyIndex(frequency, modulation);

        return IntStream.range(frequencyIndex - distance, frequencyIndex + distance).boxed().toList().stream().map(index -> {
            Frequency found = FREQUENCIES.get(index);
            if (found != null) return Pair.of(index - frequencyIndex, found);
            return null;
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public List<Integer> within(String frequency, Frequency.Modulation modulation, int distance) {
        int index = getFrequencyIndex(frequency, modulation);

        return IntStream.range(index - distance, index + distance).boxed().toList();
    }

    public static int getFrequencyIndex(String frequency, Frequency.Modulation modulation) {
        int rawFrequency = Integer.parseInt(frequency.replaceAll("[.]", ""));
        return modulation == Frequency.Modulation.AMPLITUDE ? -rawFrequency : rawFrequency;
    }

    @Override
    @Nullable
    public Frequency fromTag(CompoundTag tag) {
        if (!tag.contains("frequency") || !tag.contains("modulation")) return null;

        Frequency.Modulation modulation = modulationOf(tag.getString("modulation"));
        if (modulation == null) return null;

        return getOrCreate(tag.getString("frequency"), modulation);
    }

    public static void onLexiconRevision() {
        FREQUENCY_DIGITS = SimpleRadioLibrary.SERVER_CONFIG.frequency.wholePlaces + SimpleRadioLibrary.SERVER_CONFIG.frequency.decimalPlaces;
        MAX_FREQUENCY = (int) java.lang.Math.pow(10, FREQUENCY_DIGITS);
        FREQUENCY_PATTERN = "^\\d{"+ SimpleRadioLibrary.SERVER_CONFIG.frequency.wholePlaces+"}.\\d{"+ SimpleRadioLibrary.SERVER_CONFIG.frequency.decimalPlaces+"}$";

        if (SimpleRadioLibrary.SERVER_CONFIG.frequency.defaultFrequency.equals("auto-generate")) {
            DEFAULT_FREQUENCY = "0".repeat(SimpleRadioLibrary.SERVER_CONFIG.frequency.wholePlaces)+"."+"0".repeat(SimpleRadioLibrary.SERVER_CONFIG.frequency.decimalPlaces);
        } else {
            DEFAULT_FREQUENCY = SimpleRadioLibrary.SERVER_CONFIG.frequency.defaultFrequency;
        }
    }

    public void garbageCollect() {
        for (Frequency frequency : FREQUENCIES.values()) {
            FrequencyChannel frequencyChannel = (FrequencyChannel) frequency;
            RadioManager.validate(frequencyChannel.receivers);
            RadioManager.validate(frequencyChannel.transmitters);
        }

        FREQUENCIES.entrySet().removeIf(entry -> !((FrequencyChannel) entry.getValue()).validate());
    }
}
