package com.codinglitch.simpleradio.api.central;

import com.codinglitch.lexiconfig.annotations.LexiconEntry;
import net.minecraft.resources.ResourceLocation;

public class FrequencingType {
    public ResourceLocation location;
    public final short id;

    public int receptionPower;
    public int receptionFloor;

    public int antennaAptitude;

    public int transmissionPowerFM;
    public int diminishThresholdFM;

    public int transmissionPowerAM;
    public int diminishThresholdAM;

    public double transmissionDiminishment;

    public FrequencingType(short id) {
        this.id = id;
    }

    public int getTransmissionPower(Frequency.Modulation modulation) {
        return modulation == Frequency.Modulation.AMPLITUDE ? transmissionPowerAM : transmissionPowerFM;
    }
    public int getDiminishThreshold(Frequency.Modulation modulation) {
        return modulation == Frequency.Modulation.AMPLITUDE ? diminishThresholdAM : diminishThresholdFM;
    }
}
