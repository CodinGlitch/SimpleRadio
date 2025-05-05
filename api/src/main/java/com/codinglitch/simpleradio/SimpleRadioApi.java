package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.routers.Router;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class SimpleRadioApi {

    /**
     * Gets a config entry from a specified path
     * <p>
     * To get the <b>soundListening</b> entry from the <b>router</b> config:
     * <pre>{@code ServerSimpleRadioApi.getInstance().getConfig("router/soundListening");}</pre>
     * @param path The path to the config entry
     * @return An optional containing the value, if found
     */
    public abstract <T> Optional<T> getConfig(String path);

    /**
     * Sets a config entry from a specified path
     * <p>
     * To set the <b>soundListening</b> entry from the <b>router</b> config:
     * <pre>{@code ServerSimpleRadioApi.getInstance().getConfig("router/soundListening", true);}</pre>
     * @param path The path to the config entry
     * @param value The value to set the config entry to
     */
    public abstract <T> void setConfig(String path, T value);

    public static Router getRouterSided(UUID reference, boolean isClient) {
        return isClient ? ClientSimpleRadioApi.getInstance().getRouter(reference) : ServerSimpleRadioApi.getInstance().getRouter(reference);
    }

    public static Router getRouterSided(UUID reference, @Nullable String type, boolean isClient) {
        return isClient ? ClientSimpleRadioApi.getInstance().getRouter(reference, type) : ServerSimpleRadioApi.getInstance().getRouter(reference, type);
    }

    public static void registerRouterSided(Router router, boolean isClient, @Nullable Frequency frequency) {
        if (isClient) {
            ClientSimpleRadioApi.getInstance().registerRouter(router);
        } else {
            ServerSimpleRadioApi.getInstance().registerRouter(router, frequency);
        }
    }

    public static void removeRouterSided(UUID uuid, boolean isClient) {
        if (isClient) {
            ClientSimpleRadioApi.getInstance().removeRouter(uuid);
        } else {
            ServerSimpleRadioApi.getInstance().removeRouter(uuid);
        }
    }

    public static void removeRouterSided(Router router, boolean isClient) {
        if (isClient) {
            ClientSimpleRadioApi.getInstance().removeRouter(router);
        } else {
            ServerSimpleRadioApi.getInstance().removeRouter(router);
        }
    }
}
