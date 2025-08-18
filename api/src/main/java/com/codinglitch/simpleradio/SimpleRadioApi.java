package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class SimpleRadioApi {

    /**
     * Gets a config entry from a specified path
     * <p>
     * To get the <b>soundListening</b> entry from the <b>router</b> config:
     * <pre>{@code ServerSimpleRadioApi.getInstance().getConfig("router.soundListening");}</pre>
     * @param path The path to the config entry
     * @return An optional containing the value, if found
     */
    public abstract <T> Optional<T> getConfig(String path);

    /**
     * Sets a config entry from a specified path
     * <p>
     * To set the <b>soundListening</b> entry from the <b>router</b> config:
     * <pre>{@code ServerSimpleRadioApi.getInstance().getConfig("router.soundListening", true);}</pre>
     * @param path The path to the config entry
     * @param value The value to set the config entry to
     */
    public abstract <T> void setConfig(String path, T value);

    public abstract BlockPos travelExtension(BlockPos pos, LevelAccessor level);

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

    public static Router removeRouterSided(UUID uuid, boolean isClient) {
        if (isClient) {
            return ClientSimpleRadioApi.getInstance().removeRouter(uuid);
        } else {
            return ServerSimpleRadioApi.getInstance().removeRouter(uuid);
        }
    }

    public static Router removeRouterSided(Router router, boolean isClient) {
        if (isClient) {
            return ClientSimpleRadioApi.getInstance().removeRouter(router);
        } else {
            return ServerSimpleRadioApi.getInstance().removeRouter(router);
        }
    }

    public static Router removeRouterSided(WorldlyPosition position, boolean isClient) {
        if (isClient) {
            return ClientSimpleRadioApi.getInstance().removeRouter(position);
        } else {
            return ServerSimpleRadioApi.getInstance().removeRouter(position);
        }
    }
}
