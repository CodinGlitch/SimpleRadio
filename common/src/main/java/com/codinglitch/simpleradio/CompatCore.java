package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.CompatibilityInstance;
import com.codinglitch.simpleradio.compat.VibrativeCompat;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSource;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class CompatCore {
    public static boolean initialized = false;

    public static CompatibilityInstance VC_INTERACTION = new CompatibilityInstance(
            "Voice Chat Interaction", "vcinteraction", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.voice_chat_interaction
    );
    public static CompatibilityInstance VIBRATIVE_VOICE = new CompatibilityInstance(
            "Vibrative Voice", "vibrativevoice", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.vibrative_voice,
            List.of(VC_INTERACTION)
    );
    public static CompatibilityInstance VALKYRIEN_SKIES = new CompatibilityInstance(
            "Valkyrien Skies", "valkyrienskies", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.valkyrien_skies
    );
    public static CompatibilityInstance CREATE = new CompatibilityInstance(
            "Create", "create", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.create, "[6.0,)"
    );
    public static CompatibilityInstance COMPUTER_CRAFT = new CompatibilityInstance(
            "CC:Tweaked", "computercraft", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.create
    );

    public static void postInitialize() {
        Services.COMPAT.postInitialize();
    }

    public static void spoutCompatibilities() {
        VC_INTERACTION.spout();
        VIBRATIVE_VOICE.spout();

        VALKYRIEN_SKIES.spout();
        CREATE.spout();
        COMPUTER_CRAFT.spout();

        if (!initialized) {
            initialized = true;

            CompatCore.postInitialize();
        }
        Services.COMPAT.postCompatibilityLoad();
    }

    public static void reloadCompatibilities() {
        CommonSimpleRadio.info("Reloading compatibilities!");
        spoutCompatibilities();
    }

    public static void onData(RadioSpeaker channel, RadioSource source, short[] decoded) {
        // ---- Vibrative Voice ---- \\
        if (CompatCore.VIBRATIVE_VOICE.enabled) {
            VibrativeCompat.onData(channel, source, decoded);
        }
    }

    public static RadioManager.CollectionResult verifyLocationCollection(WorldlyPosition position, Class<?> clazz) {
        RadioManager.CollectionResult result = Services.COMPAT.verifyLocationCollection(position, clazz);
        if (result == RadioManager.CollectionResult.IGNORE || result == RadioManager.CollectionResult.COLLECT) {
            return result;
        }

        return RadioManager.CollectionResult.PASS;
    }

    public static RadioManager.CollectionResult verifyEntityCollection(Entity entity, Predicate<ItemStack> inventoryCriteria) {
        RadioManager.CollectionResult result = Services.COMPAT.verifyEntityCollection(entity, inventoryCriteria);
        if (result == RadioManager.CollectionResult.IGNORE || result == RadioManager.CollectionResult.COLLECT) {
            return result;
        }

        return RadioManager.CollectionResult.PASS;
    }
}
