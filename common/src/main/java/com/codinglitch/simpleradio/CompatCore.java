package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.compat.VibrativeCompat;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class CompatCore {
    public static boolean VC_INTERACTION = false;
    public static boolean VIBRATIVE_VOICE = false;
    public static boolean VALKYRIEN_SKIES = false;
    public static boolean CREATE = false;

    public static void spoutCompatibilities() {
        //---- Voice Chat Interaction ----\\
        if (Services.PLATFORM.isModLoaded("vcinteraction")) {
            CommonSimpleRadio.info("Voice Chat Interaction is present!");
            if (SimpleRadioLibrary.SERVER_CONFIG.compatibilities.voice_chat_interaction.enabled) {
                VC_INTERACTION = true;
                CommonSimpleRadio.info("..and compat is enabled!");
            } else {
                CommonSimpleRadio.info("..but compat is disabled");
            }
        }

        //---- Vibrative Voice ----\\
        if (Services.PLATFORM.isModLoaded("vibrativevoice")) {
            CommonSimpleRadio.info("Vibrative Voice is present!");
            if (Services.PLATFORM.isModLoaded("vcinteraction")) {
                CommonSimpleRadio.info("..but so is Voice Chat Interaction?!");
            } else {
                if (SimpleRadioLibrary.SERVER_CONFIG.compatibilities.vibrative_voice.enabled) {
                    VIBRATIVE_VOICE = true;
                    CommonSimpleRadio.info("..and compat is enabled!");
                } else {
                    CommonSimpleRadio.info("..but compat is disabled");
                }
            }
        }

        //---- Valkyrien Skies ----\\
        if (Services.PLATFORM.isModLoaded("valkyrienskies")) {
            CommonSimpleRadio.info("Valkyrien Skies is present!");
            if (SimpleRadioLibrary.SERVER_CONFIG.compatibilities.valkyrien_skies.enabled) {
                VALKYRIEN_SKIES = true;
                CommonSimpleRadio.info("..and compat is enabled!");
            } else {
                CommonSimpleRadio.info("..but compat is disabled");
            }
        }

        //---- Create ----\\
        if (Services.PLATFORM.isModLoaded("create")) {
            CommonSimpleRadio.info("Create is present!");
            if (SimpleRadioLibrary.SERVER_CONFIG.compatibilities.create.enabled) {
                CREATE = true;
                CommonSimpleRadio.info("..and compat is enabled!");
            } else {
                CommonSimpleRadio.info("..but compat is disabled");
            }
        }

        Services.COMPAT.postCompatibilityLoad();
    }

    public static void reloadCompatibilities() {
        CommonSimpleRadio.info("Reloading compatibilities!");
        spoutCompatibilities();
    }

    public static void onData(RadioSpeaker channel, RadioSource source, short[] decoded) {
        // ---- Vibrative Voice ---- \\
        if (CompatCore.VIBRATIVE_VOICE) {
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
