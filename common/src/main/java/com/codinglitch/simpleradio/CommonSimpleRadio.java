package com.codinglitch.simpleradio;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonSimpleRadio {
    public static final String ID = "simpleradio";
    public static ResourceLocation id(String id) {
        return new ResourceLocation(CommonSimpleRadio.ID, id);
    }

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