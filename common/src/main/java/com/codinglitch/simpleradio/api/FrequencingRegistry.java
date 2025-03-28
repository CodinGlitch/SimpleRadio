package com.codinglitch.simpleradio.api;

import com.codinglitch.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioServerConfig;
import com.codinglitch.simpleradio.api.central.FrequencingType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class FrequencingRegistry {
    private static short id = 0;
    private static final HashMap<Short, FrequencingType> FREQUENCING_TYPES = new HashMap<>();

    public static FrequencingType get(ResourceLocation location) {
        return FREQUENCING_TYPES.values().stream().filter(type -> type.location.equals(location)).findFirst().orElse(null);
    }

    public static FrequencingType getById(short id) {
        return FREQUENCING_TYPES.get(id);
    }

    public static FrequencingType fromConfig(LexiconPageData page) {
        FrequencingType newType = new FrequencingType();

        newType.receptionPower = (int) page.getEntry("receptionPower").orElse(-1);
        newType.receptionFloor = (int) page.getEntry("receptionFloor").orElse(-1);

        newType.antennaAptitude = (int) page.getEntry("antennaAptitude").orElse(-1);

        newType.transmissionPowerFM = (int) page.getEntry("transmissionPowerFM").orElse(-1);
        newType.diminishThresholdFM = (int) page.getEntry("diminishThresholdFM").orElse(-1);

        newType.transmissionPowerAM = (int) page.getEntry("transmissionPowerAM").orElse(-1);
        newType.diminishThresholdAM = (int) page.getEntry("diminishThresholdAM").orElse(-1);

        newType.transmissionDiminishment = (double) page.getEntry("transmissionDiminishment").orElse(-1d);

        return newType;
    }

    public static FrequencingType register(ResourceLocation location, FrequencingType frequencingType) {
        frequencingType.location = location;
        frequencingType.id = id++;

        FREQUENCING_TYPES.put(frequencingType.id, frequencingType);
        return frequencingType;
    }
}
