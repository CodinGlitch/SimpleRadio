package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.CuriosCompat;
import com.codinglitch.simpleradio.compat.etched.EtchedCompat;
import com.codinglitch.simpleradio.compat.CCCompat;
import com.codinglitch.simpleradio.compat.create.CreateCompat;
import com.codinglitch.simpleradio.platform.services.CompatPlatform;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioMessage;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class ForgeCompatPlatform implements CompatPlatform {
    @Override
    public void onData(RadioSpeaker channel, RadioMessage message, short[] decoded) {

    }

    @Override
    public String getSound(ItemStack stack) {
        // ---- Etched ---- \\
        if (CompatCore.ETCHED.enabled) {
            String result = EtchedCompat.getSound(stack);
            if (result != null) return result;
        }

        return null;
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

        // Prevent router collection if it's in a curio slot
        if (CompatCore.CURIOS.isLoaded && entity instanceof Player player) {
            ItemStack stack = CuriosCompat.getAccessory(player, inventoryCriteria);
            if (stack != ItemStack.EMPTY) return RadioManager.CollectionResult.IGNORE;
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

        if (CompatCore.COMPUTER_CRAFT.isLoaded) {
            CCCompat.postInitialize();
        }

        if (CompatCore.CURIOS.isLoaded) {
            CuriosCompat.postInitialize();
        }
    }

    @Override
    public ItemStack getAccessory(Player player, Predicate<ItemStack> filter) {
        if (CompatCore.CURIOS.isLoaded) {
            return CuriosCompat.getAccessory(player, filter);
        }

        return ItemStack.EMPTY;
    }
}