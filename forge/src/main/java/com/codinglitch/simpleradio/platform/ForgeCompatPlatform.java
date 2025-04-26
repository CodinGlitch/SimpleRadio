package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.compat.create.CreateCompat;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.platform.services.CompatPlatform;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;
import com.mojang.math.Quaternion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class ForgeCompatPlatform implements CompatPlatform {
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
    public Quaternion modifyRotation(WorldlyPosition position, Quaternion rotation) {

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
            RadioManager.CollectionResult result = CreateCompat.verifyContraptionCollection(entity);
            if (result == RadioManager.CollectionResult.IGNORE || result == RadioManager.CollectionResult.COLLECT) {
                return result;
            }
        }

        return RadioManager.CollectionResult.PASS;
    }

    @Override
    public void postCompatibilityLoad() {
        if (CompatCore.CREATE.enabled) {
            CreateCompat.registerMovementBehaviours();
        }
    }

    @Override
    public void postInitialize() {
        if (CompatCore.CREATE.isLoaded && CompatCore.CREATE.fitsVersion) {
            CreateCompat.postInitialize();
        }
    }
}