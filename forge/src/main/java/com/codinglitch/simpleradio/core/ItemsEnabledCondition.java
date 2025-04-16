package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;

public class ItemsEnabledCondition implements ICondition {
    private static final ResourceLocation NAME = CommonSimpleRadio.id("items_enabled");
    private final String item;

    public ItemsEnabledCondition(String item) {
        this.item = item;
    }

    @Override
    public ResourceLocation getID()
    {
        return NAME;
    }

    @Override
    public boolean test(IContext context) {
        return SimpleRadioItems.getByName(item).enabled;
    }

    @Override
    public String toString() {
        return "item_enabled(\"" + item + "\")";
    }

    public static class Serializer implements IConditionSerializer<ItemsEnabledCondition>
    {
        public static final ItemsEnabledCondition.Serializer INSTANCE = new ItemsEnabledCondition.Serializer();

        @Override
        public void write(JsonObject json, ItemsEnabledCondition value) {
            json.addProperty("item", value.item);
        }

        @Override
        public ItemsEnabledCondition read(JsonObject json) {
            return new ItemsEnabledCondition(GsonHelper.getAsString(json, "item"));
        }

        @Override
        public ResourceLocation getID()
        {
            return ItemsEnabledCondition.NAME;
        }
    }
}