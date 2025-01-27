package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.datagen.CommonRecipeProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShapedRecipeBuilder.Result.class)
public class MixinShapedRecipeResult {

    @Inject(at = @At("HEAD"), method = "serializeRecipeData")
    private void simpleradio$serializeRecipeData(JsonObject jsonObject, CallbackInfo ci) {
        if (CommonRecipeProvider.MAP.containsKey(this)) {
            ResourceLocation location = CommonRecipeProvider.MAP.get(this);

            JsonObject forge = new JsonObject();
            forge.addProperty("type", "simpleradio:items_enabled");
            forge.addProperty("item", location.getPath());
            jsonObject.add("forge:condition", forge);

            JsonArray neoForge = new JsonArray();
            JsonObject condition = new JsonObject();
            condition.addProperty("type", "simpleradio:items_enabled");
            condition.addProperty("item", location.getPath());
            neoForge.add(condition);
            jsonObject.add("neoforge:conditions", neoForge);
        }
    }
}