package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.ConfigHolder;
import com.codinglitch.simpleradio.platform.Services;

import java.util.List;

public class CompatibilityInstance {
    public final String name;
    public final String modId;
    public final CompatibilityConfig config;
    public final List<CompatibilityInstance> incompatibleWith;
    public final String compatibleFor;

    public boolean enabled = false;

    public boolean isLoaded = false;
    public boolean fitsVersion = false;

    public CompatibilityInstance(String name, String modId, CompatibilityConfig config, List<CompatibilityInstance> incompatibleWith, String compatibleFor) {
        this.name = name;
        this.modId = modId;
        this.config = config;

        this.incompatibleWith = incompatibleWith;
        this.compatibleFor = compatibleFor;
    }

    public CompatibilityInstance(String name, String modId, CompatibilityConfig config, List<CompatibilityInstance> incompatibleWith) {
        this(name, modId, config, incompatibleWith, null);
    }

    public CompatibilityInstance(String name, String modId, CompatibilityConfig config, String compatibleFor) {
        this(name, modId, config, List.of(), compatibleFor);
    }

    public CompatibilityInstance(String name, String modId, CompatibilityConfig config) {
        this(name, modId, config, List.of(), null);
    }


    public boolean isLoaded() {
        return Services.PLATFORM.isModLoaded(this.modId);
    }

    public void spout() {
        this.enabled = false;

        if (isLoaded()) {
            this.isLoaded = true;
            CommonSimpleRadio.info("{} is present!", name);

            for (CompatibilityInstance instance : incompatibleWith) {
                if (instance.isLoaded()) {
                    CommonSimpleRadio.info("..but so is {}?!", instance.name);
                    return;
                }
            }

            String version = Services.PLATFORM.getModVersion(this.modId);
            if (this.compatibleFor != null && !Services.PLATFORM.isVersionWithin(version, this.compatibleFor)) {
                CommonSimpleRadio.info("..but it is not a supported version!");
                this.fitsVersion = false;
                return;
            }
            this.fitsVersion = true;

            if (this.config.isEnabled()) {
                this.enabled = true;
                CommonSimpleRadio.info("..and compat is enabled!");
            } else {
                CommonSimpleRadio.info("..but compat is disabled!");
            }
        }
    }

    // interface for uhh idk something
    public interface CompatibilityConfig extends ConfigHolder {
        boolean isEnabled();
    }
}
