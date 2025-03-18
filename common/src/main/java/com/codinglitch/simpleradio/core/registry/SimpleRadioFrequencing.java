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
    public static FrequencingType WALKIE_TALKIE = FrequencingRegistry.register(
            CommonSimpleRadio.id("walkie_talkie"),
            FrequencingRegistry.fromConfig(SimpleRadioLibrary.SERVER_CONFIG.walkie_talkie)
    );
    public static FrequencingType TRANSCEIVER = FrequencingRegistry.register(
            CommonSimpleRadio.id("transceiver"),
            FrequencingRegistry.fromConfig(SimpleRadioLibrary.SERVER_CONFIG.transceiver)
    );

    public static FrequencingType TRANSMITTER = FrequencingRegistry.register(
            CommonSimpleRadio.id("transmitter"),
            FrequencingRegistry.fromConfig(SimpleRadioLibrary.SERVER_CONFIG.transmitter)
    );
    public static FrequencingType RECEIVER = FrequencingRegistry.register(
            CommonSimpleRadio.id("receiver"),
            FrequencingRegistry.fromConfig(SimpleRadioLibrary.SERVER_CONFIG.receiver)
    );

    public static FrequencingType RADIO = FrequencingRegistry.register(
            CommonSimpleRadio.id("radio"),
            FrequencingRegistry.fromConfig(SimpleRadioLibrary.SERVER_CONFIG.radio)
    );

    public static void load() {}
}
