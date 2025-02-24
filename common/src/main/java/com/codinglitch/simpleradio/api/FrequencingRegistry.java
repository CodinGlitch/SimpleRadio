package com.codinglitch.simpleradio.api;

import com.codinglitch.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioServerConfig;
import com.codinglitch.simpleradio.api.central.FrequencingType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class FrequencingRegistry {
    private static final HashMap<ResourceLocation, FrequencingType> FREQUENCING_TYPES = new HashMap<>();

    public static FrequencingType get(ResourceLocation location) {
        return FREQUENCING_TYPES.get(location);
    }

    public static FrequencingType fromConfig(LexiconPageData page) {
        FrequencingType newType = new FrequencingType();

        // why didnt i make this an optional :(
        Object receptionPower = page.getEntry("receptionPower");
        newType.receptionPower = (int) (receptionPower != null ? receptionPower : -1);
        Object receptionFloor = page.getEntry("receptionFloor");
        newType.receptionFloor = (int) (receptionFloor != null ? receptionFloor : -1);

        Object antennaAptitude = page.getEntry("antennaAptitude");
        newType.antennaAptitude = (int) (antennaAptitude != null ? antennaAptitude : -1);

        Object transmissionPowerFM = page.getEntry("transmissionPowerFM");
        newType.transmissionPowerFM = (int) (transmissionPowerFM != null ? transmissionPowerFM : -1);
        Object diminishThresholdFM = page.getEntry("diminishThresholdFM");
        newType.diminishThresholdFM = (int) (diminishThresholdFM != null ? diminishThresholdFM : -1);

        Object transmissionPowerAM = page.getEntry("transmissionPowerAM");
        newType.transmissionPowerAM = (int) (transmissionPowerAM != null ? transmissionPowerAM : -1);
        Object diminishThresholdAM = page.getEntry("diminishThresholdAM");
        newType.diminishThresholdAM = (int) (diminishThresholdAM != null ? diminishThresholdAM : -1);

        Object transmissionDiminishment = page.getEntry("transmissionDiminishment");
        newType.transmissionDiminishment = (double) (transmissionDiminishment != null ? transmissionDiminishment : -1d);

        return newType;
    }

    public static FrequencingType register(ResourceLocation location, FrequencingType frequencingType) {
        frequencingType.location = location;

        FREQUENCING_TYPES.put(location, frequencingType);
        return frequencingType;
    }
}
