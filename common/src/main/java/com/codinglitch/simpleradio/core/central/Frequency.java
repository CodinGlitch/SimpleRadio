package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.radio.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.*;
import java.util.function.Predicate;

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

    public static String DEFAULT_FREQUENCY;
    public static Modulation DEFAULT_MODULATION = Modulation.FREQUENCY;
    public static int FREQUENCY_DIGITS;
    public static int MAX_FREQUENCY;
    public static String FREQUENCY_PATTERN;

    public boolean isValid = true;

    public final Modulation modulation;
    public final String frequency;
    public final List<RadioReceiver> receivers;
    public final List<RadioTransmitter> transmitters; //TODO: create transmitter class and replace this

    public Frequency(String frequency, Modulation modulation) {
        if (!check(frequency)) {
            CommonSimpleRadio.warn("{} does not follow frequency pattern! Replacing with default pattern {}", frequency, DEFAULT_FREQUENCY);
            frequency = DEFAULT_FREQUENCY;
        }

        this.frequency = frequency;
        this.modulation = modulation;
        this.receivers = new ArrayList<>();
        this.transmitters = new ArrayList<>();

        frequencies.add(this);
    }

    public static Frequency tryParse(String string) {
        Modulation modulation = modulationOf(string.substring(string.length() - 2));
        return getOrCreateFrequency(string.substring(0, string.length() - 2), modulation);
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

    static {
        onLexiconRevision(); // stupid
    }

    public static void garbageCollect() {
        for (Frequency frequency : frequencies) {
            frequency.receivers.removeIf(Predicate.not(RadioReceiver::validate));
            frequency.transmitters.removeIf(Predicate.not(RadioTransmitter::validate));
        }

        frequencies.removeIf(Predicate.not(Frequency::validate));
    }

    @Nullable
    public static Modulation modulationOf(String shorthand) {
        for (Modulation modulation : Modulation.values())
            if (modulation.shorthand.equals(shorthand)) return modulation;
        return null;
    }

    public static boolean check(String frequency) {
        return frequency.matches(FREQUENCY_PATTERN);
    }

    public static String incrementFrequency(String frequency, int amount) {
        int rawFrequency = Integer.parseInt(frequency.replaceAll("[.]", ""));
        String str = String.format("%0"+FREQUENCY_DIGITS+"d", Math.clamp(0, MAX_FREQUENCY-1, rawFrequency + amount));
        return new StringBuilder(str).insert(str.length() - SimpleRadioLibrary.SERVER_CONFIG.frequency.decimalPlaces, ".").toString();
    }

    public static List<Frequency> getFrequencies() {
        return frequencies;
    }

    public static int getFrequencyIndex(String string, Modulation modulation) {
        for (int i = 0; i < frequencies.size(); i++) {
            Frequency frequency = frequencies.get(i);
            if (frequency.frequency.equals(string) && frequency.modulation.equals(modulation))
                return i;
        }

        return -1;
    }

    public static Frequency getFrequency(String string, Modulation modulation) {
        for (int i = 0; i < frequencies.size(); i++) {
            Frequency frequency = frequencies.get(i);
            if (frequency.frequency.equals(string) && frequency.modulation.equals(modulation))
                return frequency;
        }

        return null;
    }

    //---- Receivers ----\\

    public RadioReceiver getReceiver(Predicate<RadioReceiver> criteria) {
        return receivers.stream().filter(criteria).findFirst().orElse(null);
    }
    public RadioReceiver getReceiver(WorldlyPosition location) {
        return getReceiver(receiver -> receiver.location == location);
    }
    public RadioReceiver getReceiver(Entity owner) {
        return getReceiver(receiver -> receiver.owner == owner);
    }
    public RadioReceiver getReceiver(UUID id) {
        return getReceiver(receiver -> receiver.id.equals(id));
    }

    public RadioReceiver addReceiver(RadioReceiver receiver) {
        receivers.add(receiver);
        CommonSimpleRadio.debug("Added receiver {} to frequency {}", receiver.id, this.frequency);
        return receiver;
    }

    public RadioReceiver tryAddReceiver(UUID id, WorldlyPosition location) {
        RadioReceiver receiver = getReceiver(id);
        if (receiver == null)
            return addReceiver(id, location);

        CommonSimpleRadio.info("Failed to add receiver {} to frequency {} as they already exist", id, this.frequency);
        return receiver;
    }
    public RadioReceiver addReceiver(UUID id, WorldlyPosition location) {
        return addReceiver(new RadioReceiver(this,  location, id));
    }

    public RadioReceiver tryAddReceiver(UUID id, Entity entity) {
        RadioReceiver receiver = getReceiver(id);
        if (receiver == null)
            return addReceiver(id, entity);

        CommonSimpleRadio.info("Failed to add receiver {} to frequency {} as they already exist", id, this.frequency);
        return receiver;
    }
    public RadioReceiver addReceiver(UUID id, Entity entity) {
        return addReceiver(new RadioReceiver(this,  entity, id));
    }

    public void removeReceiver(RadioReceiver transmitter) {
        receivers.remove(transmitter);

        if (!this.validate())
            frequencies.remove(this);
    }
    public void removeReceiver(Predicate<RadioReceiver> criteria) {
        receivers.stream().filter(criteria).findFirst().ifPresent(this::removeReceiver);
    }
    public void removeReceiver(Entity owner) {
        removeReceiver(receiver -> receiver.owner == owner);
    }
    public void removeReceiver(WorldlyPosition location) {
        removeReceiver(receiver -> receiver.location == location);
    }
    public void removeReceiver(UUID id) {
        removeReceiver(receiver -> receiver.id == id);
    }

    //---- Transmitters ----\\

    public RadioTransmitter getTransmitter(Predicate<RadioTransmitter> criteria) {
        return transmitters.stream().filter(criteria).findFirst().orElse(null);
    }
    public RadioTransmitter getTransmitter(WorldlyPosition location) {
        return getTransmitter(transmitter -> transmitter.location == location);
    }
    public RadioTransmitter getTransmitter(Entity owner) {
        return getTransmitter(transmitter -> transmitter.owner == owner);
    }
    public RadioTransmitter getTransmitter(UUID id) {
        return getTransmitter(transmitter -> transmitter.id == id);
    }

    public RadioTransmitter addTransmitter(RadioTransmitter transmitter) {
        transmitters.add(transmitter);
        CommonSimpleRadio.debug("Added transmitter {} to frequency {}", transmitter.id, this.frequency);
        return transmitter;
    }

    public RadioTransmitter tryAddTransmitter(UUID id, WorldlyPosition location) {
        RadioTransmitter transmitter = getTransmitter(id);
        if (transmitter == null)
            return addTransmitter(id, location);

        CommonSimpleRadio.info("Failed to add transmitter {} to frequency {} as they already exist", id, this.frequency);
        return transmitter;
    }
    public RadioTransmitter addTransmitter(UUID id, WorldlyPosition location) {
        return addTransmitter(new RadioTransmitter(this, location, id));
    }

    public RadioTransmitter tryAddTransmitter(UUID id, Entity entity) {
        RadioTransmitter transmitter = getTransmitter(id);
        if (transmitter == null)
            return addTransmitter(id, entity);

        CommonSimpleRadio.info("Failed to add transmitter {} to frequency {} as they already exist", id, this.frequency);
        return transmitter;
    }
    public RadioTransmitter addTransmitter(UUID id, Entity entity) {
        return addTransmitter(new RadioTransmitter(this, entity, id));
    }

    public void removeTransmitter(RadioTransmitter transmitter) {
        transmitters.remove(transmitter);

        if (!this.validate())
            frequencies.remove(this);
    }
    public void removeTransmitter(Predicate<RadioTransmitter> criteria) {
        transmitters.stream().filter(criteria).findFirst().ifPresent(this::removeTransmitter);
    }
    public void removeTransmitter(Entity owner) {
        removeTransmitter(transmitter -> transmitter.owner == owner);
    }
    public void removeTransmitter(WorldlyPosition location) {
        removeTransmitter(transmitter -> transmitter.location == location);
    }
    public void removeTransmitter(UUID id) {
        removeTransmitter(transmitter -> transmitter.id == id);
    }

    public void serverTick(int tickCount) {}

    public boolean validate() {
        if (this.receivers.isEmpty() && this.transmitters.isEmpty()) {
            this.invalidate();
            return false;
        }

        return true;
    }

    public void invalidate() {
        this.isValid = false;
    }

    public Frequency revalidate() {
        return Frequency.getFrequency(this.frequency, this.modulation);
    }

    public static Frequency getOrCreateFrequency(String frequency, Modulation modulation) {
        if (frequency.isEmpty()) frequency = DEFAULT_FREQUENCY;
        if (modulation == null) modulation = DEFAULT_MODULATION;

        int index = getFrequencyIndex(frequency, modulation);
        if (index != -1) return frequencies.get(index);

        return new Frequency(frequency, modulation);
    }

    @Nullable
    public static Frequency fromTag(CompoundTag tag) {
        if (!tag.contains("frequency") || !tag.contains("modulation")) return null;

        Modulation modulation = modulationOf(tag.getString("modulation"));
        if (modulation == null) return null;

        return Frequency.getOrCreateFrequency(tag.getString("frequency"), modulation);
    }
}
