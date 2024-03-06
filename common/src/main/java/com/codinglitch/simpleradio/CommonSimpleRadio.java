package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.lexiconfig.Lexiconfig;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ServiceLoader;

public class CommonSimpleRadio {
    public static final String ID = "simpleradio";
    public static ResourceLocation id(String id) {
        return new ResourceLocation(CommonSimpleRadio.ID, id);
    }

    public static <T> T loadService(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        CommonSimpleRadio.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    // -- Logging -- \\
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

        Lexiconfig.register(SERVER_CONFIG);
        //Lexiconfig.registerListener(Lexiconfig.Event.RELOAD, Frequency::onLexiconReload);

        Lexiconfig.reload();
    }
}