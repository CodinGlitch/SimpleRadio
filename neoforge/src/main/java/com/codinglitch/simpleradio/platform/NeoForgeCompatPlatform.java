package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.CCCompat;
import com.codinglitch.simpleradio.platform.services.CompatPlatform;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSource;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import java.util.function.Predicate;

public class NeoForgeCompatPlatform implements CompatPlatform {
    @Override
    public void onData(RadioSpeaker channel, RadioSource source, short[] decoded) {

    }

    @Override
    public WorldlyPosition modifyPosition(WorldlyPosition position) {

        // ---- Valkyrien Skies ---- \\
        if (CompatCore.VALKYRIEN_SKIES.enabled) {
        }

        return position;
    }

    @Override
    public Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {

        // ---- Valkyrien Skies ---- \\
        if (CompatCore.VALKYRIEN_SKIES.enabled) {
        }

        return rotation;
    }

    @Override
    public RadioManager.CollectionResult verifyLocationCollection(WorldlyPosition location, Class<?> clazz) {
        return RadioManager.CollectionResult.PASS;
    }

    @Override
    public RadioManager.CollectionResult verifyEntityCollection(Entity entity, Predicate<ItemStack> inventoryCriteria) {
        if (CompatCore.CREATE.enabled) {
        }

        return RadioManager.CollectionResult.PASS;
    }

    @Override
    public String getSound(ItemStack stack) {
        return "";
    }

    @Override
    public void postCompatibilityLoad() {
        if (CompatCore.CREATE.enabled) {
        }
    }

    @Override
    public void postInitialize() {
        if (CompatCore.CREATE.isLoaded && CompatCore.CREATE.fitsVersion) {
        }
    }
}