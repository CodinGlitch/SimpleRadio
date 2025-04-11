package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.platform.services.PlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import java.nio.file.Path;

public class FabricPlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public String getModVersion(String modId) {
        if (!isModLoaded(modId)) return "";
        return FabricLoader.getInstance().getModContainer(modId).get().getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public boolean isVersionWithin(String version, String set) { // goofy ahh interval notation implementation
        String[] parts = set.split(",");
        boolean fromInclusive = set.startsWith("["); boolean toInclusive = set.endsWith("]");

        try {
            Version parsedVersion = Version.parse(version);

            String fromString = parts[0].substring(1);
            String toString = parts[1].substring(0, parts[1].length()-1);

            Version from = fromString.isEmpty() ? null : Version.parse(fromString);
            Version to = toString.isEmpty() ? null : Version.parse(toString);

            if (from != null && (fromInclusive ? (parsedVersion.compareTo(from) < 0) : (parsedVersion.compareTo(from) <= 0))) return false;
            if (to != null && (toInclusive ? (parsedVersion.compareTo(to) > 0) : (parsedVersion.compareTo(to) >= 0))) return false;

            return true;
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
