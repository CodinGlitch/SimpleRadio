package com.codinglitch.simpleradio.radio;

import com.codinglitch.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import org.joml.Math;

import javax.annotation.Nullable;
import java.util.UUID;

public class RadioSource {
    public enum Type {
        TRANSCEIVER,
        WALKIE_TALKIE,
        TRANSMITTER
    }

    public UUID owner;
    public UUID originalOwner;
    public WorldlyPosition origin;
    public byte[] data;
    public Type type;

    public float volume;

    public Frequency travelledAcross;
    public double transmissionPower = -1;

    private RadioSource() {}

    public RadioSource(UUID owner, WorldlyPosition location, byte[] data, float volume) {
        this.owner = owner;
        this.origin = location;
        this.data = data;
        this.volume = volume;
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

        return modulation == Frequency.Modulation.FREQUENCY ?
                (int) pageData.getEntry(configName+"FM") :
                (int) pageData.getEntry(configName+"AM");
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
        copy.data = this.data;
        copy.type = this.type;

        copy.volume = this.volume;

        copy.travelledAcross = this.travelledAcross;
        copy.transmissionPower = this.transmissionPower;

        return copy;
    }

    public void route(RadioRouter to) {

    }

    public void travel(WorldlyPosition from, WorldlyPosition to, @Nullable Frequency across) {
        double distance = from.distance(to);
        double transmissionFactor;
        if (across == null) {
            transmissionFactor = SimpleRadioLibrary.SERVER_CONFIG.cable.transmissionDiminishment;
        } else {
            transmissionFactor = getTransmissionDiminishment();

            if (from.level.dimensionType() != to.level.dimensionType()) {
                if (SimpleRadioLibrary.SERVER_CONFIG.frequency.crossDimensional) {
                    double interference = SimpleRadioLibrary.SERVER_CONFIG.frequency.dimensionalInterference;
                    transmissionFactor += across.modulation == Frequency.Modulation.FREQUENCY ? interference : interference/2;
                } else {
                    this.transmissionPower = 0;
                    transmissionFactor = 0;
                }
            }
        }

        this.travelledAcross = across;
        this.transmissionPower = Math.max(0, this.transmissionPower - (distance * transmissionFactor));
    }

    public double computeSeverity() {

        double diminishThreshold = this.getDiminishThreshold(travelledAcross.modulation);
        float base = 0;

        if (travelledAcross != null) {
            base = travelledAcross.modulation == Frequency.Modulation.FREQUENCY ? 2 : 15;
        }

        double power = 1 - Math.clamp(0f, 1f,  this.transmissionPower / diminishThreshold);

        return Math.clamp(
                0, 100,
                base + power * (100 - base)
        );
    }
}
