package com.codinglitch.simpleradio.radio;

import com.codinglitch.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.Medium;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import net.minecraft.sounds.SoundEvent;
import org.joml.Math;

import java.util.UUID;

/**
 * A source containing the audio data as well as other data collected while travelling.
 */
public class RadioSource {
    public enum Type {
        TRANSCEIVER,
        WALKIE_TALKIE,
        TRANSMITTER
    }

    public UUID owner;
    public UUID originalOwner;
    public WorldlyPosition origin;
    public Type type;

    public byte[] data;

    public SoundEvent soundEvent;

    public float pitch = 1;
    public float volume;
    public float offset;
    public long seed;

    public Frequency frequencyMedium;
    public Wire wireMedium;

    public double transmissionPower = 50;

    protected RadioSource() {}

    public RadioSource(UUID owner, WorldlyPosition location, byte[] data, float volume) {
        this.owner = owner;
        this.origin = location;
        this.volume = volume;

        this.data = data;
    }

    public RadioSource(UUID owner, WorldlyPosition location, SoundEvent soundEvent, float volume) {
        this.owner = owner;
        this.origin = location;
        this.volume = volume;

        this.soundEvent = soundEvent;
    }

    public UUID getRealOwner() {
        return originalOwner == null ? owner : originalOwner;
    }

    public void delegate(UUID owner) {
        this.originalOwner = this.owner;
        this.owner = owner;
    }

    public void addPower(double power) {
        this.transmissionPower += power;
    }

    public LexiconPageData getPage() {
        String pageName = this.type.toString().toLowerCase();
        LexiconPageData pageData = SimpleRadioLibrary.SERVER_CONFIG.getPage(pageName);
        if (pageData == null) {
            CommonSimpleRadio.warn("Could not find page {}!", pageName);
            return null;
        }

        return pageData;
    }
    public Object getConfigFor(Frequency.Modulation modulation, String configName) {
        LexiconPageData pageData = getPage();
        if (pageData == null) return 0;

        return pageData.getEntry(configName + modulation.shorthand);
    }

    public int getTransmissionPower(Frequency.Modulation modulation) {
        return (int) getConfigFor(modulation, "transmissionPower");
    }

    public int getDiminishThreshold(Frequency.Modulation modulation) {
        return (int) getConfigFor(modulation, "transmissionPower");
    }

    public double getTransmissionDiminishment() {
        LexiconPageData pageData = getPage();
        if (pageData == null) return 1;
        return (double) pageData.getEntry("transmissionDiminishment");
    }

    public RadioSource copy() {
        RadioSource copy = new RadioSource();

        copy.owner = this.owner;
        copy.originalOwner = this.originalOwner;
        copy.origin = this.origin;
        copy.type = this.type;

        copy.data = this.data;
        copy.soundEvent = this.soundEvent;

        copy.volume = this.volume;
        copy.pitch = this.pitch;
        copy.offset = this.offset;
        copy.seed = this.seed;

        copy.frequencyMedium = this.frequencyMedium;
        copy.wireMedium = this.wireMedium;
        copy.transmissionPower = this.transmissionPower;

        return copy;
    }

    public void travel(WorldlyPosition from, WorldlyPosition to, Medium medium) {
        double distance = from.distance(to);
        double transmissionFactor = 0;
        if (medium instanceof Wire wire) {
            transmissionFactor = SimpleRadioLibrary.SERVER_CONFIG.wire.transmissionDiminishment;

            this.wireMedium = wire;
        } else if (medium instanceof Frequency frequency) {
            transmissionFactor = getTransmissionDiminishment();

            if (from.level.dimensionType() != to.level.dimensionType()) {
                if (SimpleRadioLibrary.SERVER_CONFIG.frequency.crossDimensional) {
                    double interference = SimpleRadioLibrary.SERVER_CONFIG.frequency.dimensionalInterference;
                    transmissionFactor += frequency.modulation == Frequency.Modulation.FREQUENCY ? interference : interference/2;
                } else {
                    this.transmissionPower = 0;
                    transmissionFactor = 0;
                }
            }

            this.frequencyMedium = frequency;
        }

        //TODO: fix this; currently you can just use transmitter over a short distance, which sets the transmission power and then travelling tens of thousands of blocks over wire

        this.transmissionPower = Math.max(0, this.transmissionPower - (distance * transmissionFactor));
    }

    public double computeSeverity() {
        float base = 0;

        double severity = 0;
        if (this.frequencyMedium != null) {
            double diminishThreshold = this.getDiminishThreshold(frequencyMedium.modulation);

            base = frequencyMedium.modulation == Frequency.Modulation.FREQUENCY ? 2 : 15;
            severity = 1 - Math.clamp(0f, 1f,  this.transmissionPower / diminishThreshold);
        }

        return Math.clamp(
                0, 100,
                base + severity * (100 - base)
        );
    }

    public boolean isValid() {
        return this.origin != null;
    }
}
