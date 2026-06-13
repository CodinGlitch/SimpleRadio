package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Catalyst;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;

public class SimpleRadioCatalysts extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static SimpleRadioCatalysts INSTANCE;

    public SimpleRadioCatalysts() {
        super(GSON, "radio_catalysts");
        INSTANCE = this;
    }

    public static void load() {}

    //TODO: properly handle reloading instead of rewriting it every time
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                CommonSimpleRadio.error("Catalysts should be an object!");
                continue;
            }

            ResourceLocation location = entry.getKey();
            JsonObject contents = entry.getValue().getAsJsonObject();

            Item associate = BuiltInRegistries.ITEM.get(location);
            if (associate == Items.AIR) {
                CommonSimpleRadio.error("Missing item associate for catalyst {}!", location);
                continue;
            }

            float efficiency = contents.get("efficiency").getAsFloat();
            CatalystRegistry.register(location, new Catalyst(associate).setEfficiency(efficiency));
        }
    }
}
