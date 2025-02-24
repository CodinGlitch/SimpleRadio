package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.SimpleRadioServerConfig;
import com.codinglitch.simpleradio.api.FrequencingRegistry;
import com.codinglitch.simpleradio.api.central.FrequencingType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class SimpleRadioFrequencing {
    public static FrequencingType RECEIVER = FrequencingRegistry.register(
            CommonSimpleRadio.id("receiver"),
            FrequencingRegistry.fromConfig(SimpleRadioLibrary.SERVER_CONFIG.receiver)
    );

    public static void load() {}
}
