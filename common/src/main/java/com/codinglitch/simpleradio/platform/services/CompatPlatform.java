package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;
import com.mojang.math.Quaternion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public interface CompatPlatform {
    void postInitialize();
    void postCompatibilityLoad();

    void onData(RadioSpeaker channel, RadioSource source, short[] decoded);

    WorldlyPosition modifyPosition(WorldlyPosition position);
    Quaternion modifyRotation(WorldlyPosition position, Quaternion rotation);

    RadioManager.CollectionResult verifyLocationCollection(WorldlyPosition location, Class<?> clazz);

    RadioManager.CollectionResult verifyEntityCollection(Entity entity, Predicate<ItemStack> inventoryCriteria);
}