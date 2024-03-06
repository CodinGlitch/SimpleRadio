package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import org.joml.Math;

import java.util.UUID;

public class RadioSource {
    public enum Type {
        TRANSCEIVER,
        TRANSMITTER
    }

    public UUID owner;
    public WorldlyPosition location;
    public byte[] data;
    public Type type;

    public RadioSource(Type type, UUID owner, WorldlyPosition location, byte[] data) {
        this.type = type;
        this.owner = owner;
        this.location = location;
        this.data = data;
    }

    public int getMaxDistance(Frequency.Modulation modulation) {
        CommonSimpleRadio.info(CommonSimpleRadio.SERVER_CONFIG.transceiver.maxFMDistance);

        if (type == Type.TRANSCEIVER)
            return modulation == Frequency.Modulation.FREQUENCY ?
                    CommonSimpleRadio.SERVER_CONFIG.transceiver.maxFMDistance :
                    CommonSimpleRadio.SERVER_CONFIG.transceiver.maxAMDistance;
        else if (type == Type.TRANSMITTER)
            return modulation == Frequency.Modulation.FREQUENCY ?
                    CommonSimpleRadio.SERVER_CONFIG.transmitter.maxFMDistance :
                    CommonSimpleRadio.SERVER_CONFIG.transmitter.maxAMDistance;

        return -1;
    }

    public int getFalloff(Frequency.Modulation modulation) {
        if (type == Type.TRANSCEIVER)
            return modulation == Frequency.Modulation.FREQUENCY ?
                    CommonSimpleRadio.SERVER_CONFIG.transceiver.falloffFM :
                    CommonSimpleRadio.SERVER_CONFIG.transceiver.falloffAM;
        else if (type == Type.TRANSMITTER)
            return modulation == Frequency.Modulation.FREQUENCY ?
                    CommonSimpleRadio.SERVER_CONFIG.transmitter.falloffFM :
                    CommonSimpleRadio.SERVER_CONFIG.transmitter.falloffAM;

        return -1;
    }

    public float computeSeverity(WorldlyPosition destination, Frequency destinationFrequency) {
        int maxDistance = this.getMaxDistance(destinationFrequency.modulation);
        int falloff = this.getFalloff(destinationFrequency.modulation);
        float distance = location.distance(destination);
        float base = destinationFrequency.modulation == Frequency.Modulation.FREQUENCY ? 2 : 15;

        if (location.level.dimensionType() != destination.level.dimensionType()) {
            if (CommonSimpleRadio.SERVER_CONFIG.frequency.crossDimensional)
                base += destinationFrequency.modulation == Frequency.Modulation.FREQUENCY ? CommonSimpleRadio.SERVER_CONFIG.frequency.dimensionalInterference : 0;
            else return 100;
        }

        return Math.clamp(
                0, 100,
                base + (Math.max(0, distance - falloff) / (maxDistance - falloff)) * (100 - base)
        );
    }
}
