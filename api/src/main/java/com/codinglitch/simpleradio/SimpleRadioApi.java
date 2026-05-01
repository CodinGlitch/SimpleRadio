package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.core.SimpleRadioEvent;
import com.codinglitch.simpleradio.radio.Source;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class SimpleRadioApi {
    private static SimpleRadioApi INSTANCE;
    public SimpleRadioApi() {
        INSTANCE = this;
    }

    public static SimpleRadioApi getInstance() {
        String threadName = Thread.currentThread().getName(); // ☹
        if (threadName.contains("Render")) {
            return getInstance(true);
        } else if (threadName.contains("Server")) {
            return getInstance(false);
        } else {
            return INSTANCE;
        }
    }
    public static SimpleRadioApi getInstance(boolean isClient) {
        return isClient ? ClientSimpleRadioApi.getInstance() : ServerSimpleRadioApi.getInstance();
    }

    public abstract Frequencies frequencies();

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

    /**
     *
     */
    public abstract <E extends SimpleRadioEvent> void listen(Class<E> event, Consumer<E> listener);

    public abstract BlockPos travelExtension(BlockPos pos, LevelAccessor level);

    public abstract <R extends Router> void registerRouter(R router);
    public abstract <R extends Router> void registerRouter(R router, @Nullable Frequency frequency);

    /**
     * Creates a blank Router with the specified reference.
     * @param reference The reference. If you are creating a block entity with a router, this should be equivalent to a UUID you store and save.
     * @return The created router.
     */
    public abstract Router newRouter(UUID reference);

    /**
     * Creates a blank Router at the specified location with a random UUID. Most of the time, you should instead use {@link #newRouter(UUID)} or {@link #newRouter(UUID, WorldlyPosition)}.
     * @param position The location of the router.
     * @return The created router
     */
    public abstract Router newRouter(WorldlyPosition position);

    /**
     * Creates a blank Router at the specified location with the specified reference.
     * @param reference The reference. If you are creating a block entity with a router, this should be equivalent to a UUID you store and save.
     * @param position The location of the router.
     * @return The created router
     */
    public abstract Router newRouter(UUID reference, WorldlyPosition position);

    public abstract Source newSource(UUID owner, WorldlyPosition location, byte[] data, float volume);

    public abstract void info(Object object, Object... substitutions);
    public abstract void debug(Object object, Object... substitutions);
    public abstract void warn(Object object, Object... substitutions);
    public abstract void error(Object object, Object... substitutions);

    // ---- Sided Methods ---- \\

    public static Router getRouterSided(UUID reference, boolean isClient) {
        return isClient ? ClientSimpleRadioApi.getInstance().getRouter(reference) : ServerSimpleRadioApi.getInstance().getRouter(reference);
    }

    public static Router getRouterSided(UUID reference, @Nullable String type, boolean isClient) {
        return isClient ? ClientSimpleRadioApi.getInstance().getRouter(reference, type) : ServerSimpleRadioApi.getInstance().getRouter(reference, type);
    }

    public static void registerRouterSided(Router router, boolean isClient, @Nullable Frequency frequency) {
        if (isClient) {
            ClientSimpleRadioApi.getInstance().registerRouter(router, frequency);
        } else {
            ServerSimpleRadioApi.getInstance().registerRouter(router, frequency);
        }
    }

    /**
     * Removes the given router from the <b>global map</b> given the indicated side.
     * This will consequently make this router <b>invalid</b>.
     * <p>
     * It should be noted that this does <i>not</i> remove it from the individual router maps (Listeners, Speakers, etc.)
     * @param uuid The UUID of the router to remove.
     * @param isClient Whether or not to remove it from the client or server map.
     * @return The router that was removed, if it exists.
     */
    public static Router removeRouterSided(UUID uuid, boolean isClient) {
        if (isClient) {
            return ClientSimpleRadioApi.getInstance().removeRouter(uuid);
        } else {
            return ServerSimpleRadioApi.getInstance().removeRouter(uuid);
        }
    }

    /**
     * Removes the given router from the <b>global map</b> given the indicated side.
     * This will consequently make this router <b>invalid</b>.
     * <p>
     * It should be noted that this does <i>not</i> remove it from the individual router maps (Listeners, Speakers, etc.)
     * @param router The router to remove.
     * @param isClient Whether or not to remove it from the client or server map.
     * @return The router that was removed, if it exists.
     */
    public static Router removeRouterSided(Router router, boolean isClient) {
        if (isClient) {
            return ClientSimpleRadioApi.getInstance().removeRouter(router);
        } else {
            return ServerSimpleRadioApi.getInstance().removeRouter(router);
        }
    }

    /**
     * Removes the given router from the <b>global map</b> given the indicated side.
     * This will consequently make this router <b>invalid</b>.
     * <p>
     * It should be noted that this does <i>not</i> remove it from the individual router maps (Listeners, Speakers, etc.)
     * @param position The position of the router to remove.
     * @param isClient Whether or not to remove it from the client or server map.
     * @return The router that was removed, if it exists.
     */
    public static Router removeRouterSided(WorldlyPosition position, boolean isClient) {
        if (isClient) {
            return ClientSimpleRadioApi.getInstance().removeRouter(position);
        } else {
            return ServerSimpleRadioApi.getInstance().removeRouter(position);
        }
    }
}
