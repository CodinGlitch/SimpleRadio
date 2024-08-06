package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.compat.InteractionCompat;
import com.codinglitch.simpleradio.compat.ValkyrienCompat;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.platform.services.CompatPlatform;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class FabricCompatPlatform implements CompatPlatform {
    @Override
    public void onData(RadioSpeaker channel, RadioSource source, short[] decoded) {

        // ---- Voice Chat Interaction ---- \\
        if (CompatCore.VC_INTERACTION) {
            InteractionCompat.onData(channel, source, decoded);
        }
    }

    @Override
    public WorldlyPosition modifyPosition(WorldlyPosition position) {

        // ---- Valkyrien Skies ---- \\
        if (CompatCore.VALKYRIEN_SKIES) {
            return ValkyrienCompat.modifyPosition(position);
        }

        return position;
    }

    @Override
    public void postCompatibilityLoad() {

    }

    @Override
    public RadioManager.CollectionResult verifyLocationCollection(WorldlyPosition location, Class<?> clazz) {
        return RadioManager.CollectionResult.PASS;
    }

    @Override
    public RadioManager.CollectionResult verifyEntityCollection(Entity entity, Predicate<ItemStack> inventoryCriteria) {
        return RadioManager.CollectionResult.PASS;
    }
}
