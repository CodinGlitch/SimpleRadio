package com.codinglitch.simpleradio.lexiconfig;

import com.codinglitch.simpleradio.lexiconfig.classes.LexiconData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.apache.commons.lang3.AnnotationUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

public class Lexiconfig {
    public enum Event {
        RELOAD
    }

    private static final List<LexiconData> REGISTERED_LEXICONS = new ArrayList<>();
    private static final Map<Event, Runnable> LISTENERS = new HashMap<>();

    public static void register(LexiconData lexicon) {
        REGISTERED_LEXICONS.add(lexicon);

        lexicon.load();
        lexicon.save();
    }

    public static void registerListener(Event eventType, Runnable listener) {
        LISTENERS.put(eventType, listener);
    }

    public static void reload() {
        LISTENERS.forEach((event, runnable) -> {
            if (event == Event.RELOAD) runnable.run();
        });
    }
}
