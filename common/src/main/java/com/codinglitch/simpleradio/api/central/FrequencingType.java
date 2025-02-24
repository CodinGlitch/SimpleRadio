package com.codinglitch.simpleradio.api.central;

import com.codinglitch.lexiconfig.annotations.LexiconEntry;
import net.minecraft.resources.ResourceLocation;

public class FrequencingType {
    public ResourceLocation location;

    public int receptionPower;

    public int antennaAptitude;

    public int transmissionPowerFM;
    public int diminishThresholdFM;

    public int transmissionPowerAM;
    public int diminishThresholdAM;

    public double transmissionDiminishment;
}
