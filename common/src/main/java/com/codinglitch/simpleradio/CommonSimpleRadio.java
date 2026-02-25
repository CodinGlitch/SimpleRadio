package com.codinglitch.simpleradio;

import com.codinglitch.lexiconfig.classes.LexiconData;
import com.codinglitch.lexiconfig.classes.LexiconEntryData;
import com.codinglitch.lexiconfig.classes.LexiconSubstrate;
import com.codinglitch.simpleradio.core.registry.*;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.ServiceLoader;

public class CommonSimpleRadio {
    public static final String ID = "simpleradio";
    public static ResourceLocation id(String... arguments) {
        return id("", arguments);
    }

    public static ResourceLocation id(CharSequence delimiter, String... arguments) {
        return ResourceLocation.fromNamespaceAndPath(CommonSimpleRadio.ID, String.join(delimiter, arguments));
    }

    public static <T> T loadService(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        CommonSimpleRadio.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public static <T> Optional<T> getConfigFrom(LexiconData lexicon, String path) {
        LexiconSubstrate currentSubstrate = lexicon;
        String[] paths = path.split("\\.");
        for (String name : paths) {
            Object entry = currentSubstrate.getEntry(name).orElse(null);
            if (entry instanceof LexiconSubstrate substrate) {
                currentSubstrate = substrate;
            } else {
                return Optional.of((T) entry);
            }
        }
        return Optional.empty();
    }

    public static <T> void setConfigFrom(LexiconData lexicon, String path, T value) {
        LexiconSubstrate currentSubstrate = lexicon;
        String[] paths = path.split("\\.");
        for (int i = 0; i < paths.length; i++) {
            String name = paths[i];
            if (i == paths.length-1) {
                LexiconEntryData<T> entryData = (LexiconEntryData<T>) currentSubstrate.getContents(entry -> entry.getName().equals(name)).stream().findFirst().orElse(null);
                if (entryData != null) entryData.set(value);
            } else {
                Object entry = currentSubstrate.getEntry(name).orElse(null);
                if (entry instanceof LexiconSubstrate substrate) currentSubstrate = substrate;
            }
        }
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

    public static void initialize() {
    }

    public static void load() {
        SimpleRadioEntities.load();
        SimpleRadioBlockEntities.load();
        SimpleRadioMenus.load();

        SimpleRadioCatalysts.load();
        SimpleRadioFrequencing.load();

        RadioManager.load();
    }
}