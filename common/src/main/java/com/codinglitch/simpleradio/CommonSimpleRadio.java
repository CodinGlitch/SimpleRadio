package com.codinglitch.simpleradio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonSimpleRadio {
    public static final String ID = "simpleradio";

    private static Logger LOGGER = LogManager.getLogger(ID);

    public static void info(Object object, Object... substitutions) {
        LOGGER.info(String.valueOf(object), substitutions);
    }
    public static void debug(Object object, Object... substitutions) {
        LOGGER.debug(String.valueOf(object), substitutions);
    }
    public static void warn(Object object, Object... substitutions) {
        LOGGER.warn(String.valueOf(object), substitutions);
    }

    public static void error(Object object, Object... substitutions) {
        LOGGER.error(String.valueOf(object), substitutions);
    }
    public static SimpleRadioServerConfig SERVER_CONFIG;
    public static void initialize() {
        SERVER_CONFIG = new SimpleRadioServerConfig();
    }
}