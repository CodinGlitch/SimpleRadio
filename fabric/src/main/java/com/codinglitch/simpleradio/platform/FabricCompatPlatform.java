package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.CCCompat;
import com.codinglitch.simpleradio.compat.InteractionCompat;
import com.codinglitch.simpleradio.compat.TrinketsCompat;
import com.codinglitch.simpleradio.platform.services.CompatPlatform;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSource;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import java.util.function.Predicate;

public class FabricCompatPlatform implements CompatPlatform {
    @Override
    public void onData(RadioSpeaker channel, RadioSource source, short[] decoded) {

        // ---- Voice Chat Interaction ---- \\
        if (CompatCore.VC_INTERACTION.enabled) {
            InteractionCompat.onData(channel, source, decoded);
        }
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
    public String getSound(ItemStack stack) {
        return null;
    }

    @Override
    public void postInitialize() {
        if (CompatCore.COMPUTER_CRAFT.isLoaded) {
            CCCompat.postInitialize();
        }

        if (CompatCore.TRINKETS.isLoaded) {
            TrinketsCompat.postInitialize();
        }
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

        // Prevent router collection if it's in a trinket slot
        if (CompatCore.TRINKETS.isLoaded && entity instanceof Player player) {
            ItemStack stack = TrinketsCompat.getAccessory(player, inventoryCriteria);
            if (stack != ItemStack.EMPTY) return RadioManager.CollectionResult.IGNORE;
        }

        return RadioManager.CollectionResult.PASS;
    }

    @Override
    public ItemStack getAccessory(Player player, Predicate<ItemStack> filter) {
        if (CompatCore.TRINKETS.isLoaded) {
            return TrinketsCompat.getAccessory(player, filter);
        }

        return ItemStack.EMPTY;
    }
}
