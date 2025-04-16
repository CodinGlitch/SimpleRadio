package com.codinglitch.simpleradio.platform.services;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;

import java.nio.file.Path;

public interface PlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    Path getConfigPath();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Gets the version string of a mod if it is loaded
     *
     * @param modId The mod to check if it is loaded.
     * @return The version string of the mod if it is loaded, or an empty string if it is not.
     */
    String getModVersion(String modId);

    /**
     * Gets the version string of a mod if it is loaded
     *
     * @param version The mod to check if it is loaded.
     * @return The version string of the mod if it is loaded, or an empty string if it is not.
     */
    boolean isVersionWithin(String version, String set);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }
}