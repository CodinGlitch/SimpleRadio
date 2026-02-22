package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSource;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import java.util.function.Predicate;

public interface CompatPlatform {
    void postInitialize();
    void postCompatibilityLoad();

    void onData(RadioSpeaker channel, RadioSource source, short[] decoded);

    WorldlyPosition modifyPosition(WorldlyPosition position);
    Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation);

    RadioManager.CollectionResult verifyLocationCollection(WorldlyPosition location, Class<?> clazz);
    RadioManager.CollectionResult verifyEntityCollection(Entity entity, Predicate<ItemStack> inventoryCriteria);

    String getSound(ItemStack stack);
}