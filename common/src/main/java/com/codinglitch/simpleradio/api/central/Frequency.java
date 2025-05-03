package com.codinglitch.simpleradio.api.central;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.radio.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.*;
import java.util.function.Predicate;

public class Frequency implements Medium {
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

    public final RouterContainer<RadioReceiver> receivers;
    public final RouterContainer<RadioTransmitter> transmitters;

    public Frequency(String frequency, Modulation modulation) {
        if (!check(frequency)) {
            CommonSimpleRadio.warn("{} does not follow frequency pattern! Replacing with default pattern {}", frequency, DEFAULT_FREQUENCY);
            frequency = DEFAULT_FREQUENCY;
        }

        this.frequency = frequency;
        this.modulation = modulation;
        this.receivers = new RouterContainer<>();
        this.transmitters = new RouterContainer<>();

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
        //onLexiconRevision(); // stupid maybe not need?
    }

    public static void garbageCollect() {
        for (Frequency frequency : frequencies) {
            RadioManager.validate(frequency.receivers);
            RadioManager.validate(frequency.transmitters);
        }

        frequencies.removeIf(Predicate.not(Frequency::validate));
    }

    public static void close() {
        frequencies.clear();
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
        for (Frequency frequency : frequencies) {
            if (frequency.frequency.equals(string) && frequency.modulation.equals(modulation))
                return frequency;
        }

        return null;
    }

    //---- Receivers ----\\

    public RadioReceiver getReceiver(Predicate<RadioReceiver> filter) {
        Optional<RadioReceiver> result = receivers.stream().filter(filter).findFirst();
        return result.orElse(null);
    }
    public RadioReceiver getReceiver(WorldlyPosition location) {
        return getReceiver(receiver -> location.equals(receiver.location));
    }
    public RadioReceiver getReceiver(Entity owner) {
        return getReceiver(receiver -> owner.equals(receiver.owner));
    }
    public RadioReceiver getReceiver(UUID id) {
        return getReceiver(receiver -> id.equals(receiver.reference));
    }

    public void registerReceiver(RadioReceiver receiver) {
        RadioManager.putRouter(receivers, receiver);
    }

    public RadioReceiver addReceiver(RadioReceiver receiver) {
        boolean isClient = false;
        if (receiver.location != null) isClient = receiver.location.isClientSide();
        else if (receiver.owner != null) isClient = receiver.owner.level().isClientSide;

        RadioManager.getInstance().registerRouterSided(receiver, isClient, this);

        CommonSimpleRadio.debug("Added receiver {} to frequency {}", receiver.reference, this.frequency);
        return receiver;
    }

    public RadioReceiver tryAddReceiver(UUID id, WorldlyPosition location) {
        boolean isClient = location.isClientSide();

        RadioReceiver receiver = null;//isClient ? ClientRadioManager.getReceiver(location) : getReceiver(location);
        if (receiver == null) receiver = isClient ? ClientRadioManager.getReceiver(id) : getReceiver(id);

        if (receiver == null)
            return addReceiver(id, location);

        //CommonSimpleRadio.info("Failed to add receiver {} to frequency {} as they already exist", id, this.frequency);
        return receiver;
    }
    public RadioReceiver addReceiver(UUID id, WorldlyPosition location) {
        return addReceiver(new RadioReceiver(this, location, id));
    }

    public RadioReceiver tryAddReceiver(UUID id, Entity entity) {
        boolean isClient = entity.level().isClientSide;

        RadioReceiver receiver = null;//isClient ? ClientRadioManager.getReceiver(entity) : getReceiver(entity);
        if (receiver == null) receiver = isClient ? ClientRadioManager.getReceiver(id) : getReceiver(id);

        if (receiver == null)
            return addReceiver(id, entity);

        //CommonSimpleRadio.info("Failed to add receiver {} to frequency {} as they already exist", id, this.frequency);
        return receiver;
    }
    public RadioReceiver addReceiver(UUID id, Entity entity) {
        return addReceiver(new RadioReceiver(this, entity, id));
    }

    public void removeReceiver(Predicate<RadioReceiver> criteria) {
        receivers.removeIf(criteria);

        if (!this.validate()) frequencies.remove(this);
    }
    public void removeReceiver(RadioReceiver receiver) {
        removeReceiver(receiver::equals);
    }
    public void removeReceiver(Entity owner) {
        removeReceiver(receiver -> owner.equals(receiver.owner));
    }
    public void removeReceiver(WorldlyPosition location) {
        removeReceiver(receiver -> location.equals(receiver.location));
    }
    public void removeReceiver(UUID id) {
        removeReceiver(receiver -> id.equals(receiver.reference));
    }

    //---- Transmitters ----\\

    public RadioTransmitter getTransmitter(Predicate<RadioTransmitter> filter) {
        Optional<RadioTransmitter> result = transmitters.stream().filter(filter).findFirst();
        return result.orElse(null);
    }
    public RadioTransmitter getTransmitter(WorldlyPosition location) {
        return getTransmitter(transmitter -> location.equals(transmitter.location));
    }
    public RadioTransmitter getTransmitter(Entity owner) {
        return getTransmitter(transmitter -> owner.equals(transmitter.owner));
    }
    public RadioTransmitter getTransmitter(UUID id) {
        return getTransmitter(transmitter -> id.equals(transmitter.reference));
    }

    public void registerTransmitter(RadioTransmitter transmitter) {
        RadioManager.putRouter(transmitters, transmitter);
    }

    public RadioTransmitter addTransmitter(RadioTransmitter transmitter) {
        boolean isClient = false;
        if (transmitter.location != null) isClient = transmitter.location.isClientSide();
        else if (transmitter.owner != null) isClient = transmitter.owner.level().isClientSide;

        RadioManager.getInstance().registerRouterSided(transmitter, isClient, this);

        CommonSimpleRadio.debug("Added transmitter {} to frequency {}", transmitter.reference, this.frequency);
        return transmitter;
    }

    public RadioTransmitter tryAddTransmitter(UUID id, WorldlyPosition location) {
        boolean isClient = location.isClientSide();

        RadioTransmitter transmitter = null;//isClient ? ClientRadioManager.getTransmitter(location) : getTransmitter(location);
        if (transmitter == null) transmitter = isClient ? ClientRadioManager.getTransmitter(id) : getTransmitter(id);

        if (transmitter == null)
            return addTransmitter(id, location);

        //CommonSimpleRadio.info("Failed to add transmitter {} to frequency {} as they already exist", id, this.frequency);
        return transmitter;
    }
    public RadioTransmitter addTransmitter(UUID id, WorldlyPosition location) {
        return addTransmitter(new RadioTransmitter(this, location, id));
    }

    public RadioTransmitter tryAddTransmitter(UUID id, Entity entity) {
        boolean isClient = entity.level().isClientSide;

        RadioTransmitter transmitter = null;//isClient ? ClientRadioManager.getTransmitter(entity) : getTransmitter(entity);
        if (transmitter == null) transmitter = isClient ? ClientRadioManager.getTransmitter(id) : getTransmitter(id);

        if (transmitter == null)
            return addTransmitter(id, entity);

        //CommonSimpleRadio.info("Failed to add transmitter {} to frequency {} as they already exist", id, this.frequency);
        return transmitter;
    }
    public RadioTransmitter addTransmitter(UUID id, Entity entity) {
        return addTransmitter(new RadioTransmitter(this, entity, id));
    }

    public void removeTransmitter(Predicate<RadioTransmitter> criteria) {
        transmitters.removeIf(criteria);

        if (!this.validate()) frequencies.remove(this);
    }
    public void removeTransmitter(RadioTransmitter transmitter) {
        removeTransmitter(transmitter::equals);
    }
    public void removeTransmitter(Entity owner) {
        removeTransmitter(transmitter -> owner.equals(transmitter.owner));
    }
    public void removeTransmitter(WorldlyPosition location) {
        removeTransmitter(transmitter -> location.equals(transmitter.location));
    }
    public void removeTransmitter(UUID id) {
        removeTransmitter(transmitter -> id.equals(transmitter.reference));
    }

    public void serverTick(int tickCount) {
        for (RadioTransmitter transmitter : transmitters) {
            transmitter.tick(tickCount);
        }
        for (RadioReceiver receiver : receivers) {
            receiver.tick(tickCount);
        }

        // retired the 'pending' thing it was pretty stupid in hindsight
    }

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

        Frequency found = getFrequency(frequency, modulation);
        if (found != null) return found;

        return new Frequency(frequency, modulation);
    }

    @Nullable
    public static Frequency fromTag(CompoundTag tag) {
        if (!tag.contains("frequency") || !tag.contains("modulation")) return null;

        Modulation modulation = modulationOf(tag.getString("modulation"));
        if (modulation == null) return null;

        return Frequency.getOrCreateFrequency(tag.getString("frequency"), modulation);
    }

    @Override
    public String toString() {
        return this.frequency + this.modulation.shorthand;
    }
}
