package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.compat.VibrativeCompat;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;

public class CompatCore {
    public static boolean VC_INTERACTION = false;
    public static boolean VIBRATIVE_VOICE = false;
    public static boolean VALKYRIEN_SKIES = false;

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
}
