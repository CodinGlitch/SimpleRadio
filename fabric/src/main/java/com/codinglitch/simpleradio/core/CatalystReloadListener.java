package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.SimpleRadioCatalysts;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class CatalystReloadListener extends SimpleRadioCatalysts implements IdentifiableResourceReloadListener {
    public static CatalystReloadListener INSTANCE = new CatalystReloadListener();

    @Override
    public ResourceLocation getFabricId() {
        return CommonSimpleRadio.id("catalysts");
    }
}
