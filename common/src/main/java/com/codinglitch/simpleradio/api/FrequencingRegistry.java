package com.codinglitch.simpleradio.api;

import com.codinglitch.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.api.central.FrequencingType;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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
        newType.page = page;
        newType.reload();

        return newType;
    }

    public static FrequencingType register(ResourceLocation location, FrequencingType frequencingType) {
        frequencingType.location = location;
        frequencingType.id = id++;

        FREQUENCING_TYPES.put(frequencingType.id, frequencingType);
        return frequencingType;
    }

    public static void reload() {
        for (Map.Entry<Short, FrequencingType> entry : FREQUENCING_TYPES.entrySet()) {
            entry.getValue().reload();
        }
    }
}
