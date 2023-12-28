package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonSimpleRadio {
    public static final String ID = "simpleradio";

    public static final SoundEvent RADIO_OPEN;
    public static final SoundEvent RADIO_CLOSE;

    private static Logger LOGGER = LogManager.getLogger(ID);

    public static void info(Object object, Object... substitutions) {
        LOGGER.info(String.format(String.valueOf(object), substitutions));
    }
    public static void debug(Object object, Object... substitutions) {
        LOGGER.debug(String.format(String.valueOf(object), substitutions));
    }
    public static void warn(Object object, Object... substitutions) {
        LOGGER.warn(String.format(String.valueOf(object), substitutions));
    }
    public static SimpleRadioServerConfig SERVER_CONFIG;
    public static void initialize() {
        SERVER_CONFIG = new SimpleRadioServerConfig();
    }
}