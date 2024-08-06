package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public interface CompatPlatform {
    void onData(RadioSpeaker channel, RadioSource source, short[] decoded);

    WorldlyPosition modifyPosition(WorldlyPosition position);

    void postCompatibilityLoad();

    RadioManager.CollectionResult verifyLocationCollection(WorldlyPosition location, Class<?> clazz);

    RadioManager.CollectionResult verifyEntityCollection(Entity entity, Predicate<ItemStack> inventoryCriteria);
}