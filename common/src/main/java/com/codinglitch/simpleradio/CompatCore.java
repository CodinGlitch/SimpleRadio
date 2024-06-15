package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.compat.VibrativeCompat;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioChannel;
import com.codinglitch.simpleradio.radio.RadioSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import static com.codinglitch.simpleradio.platform.Services.COMPAT;

public class CompatCore {
    public static boolean VC_INTERACTION = false;
    public static boolean VIBRATIVE_VOICE = false;
    public static boolean VALKYRIEN_SKIES = false;

    public static void spoutCompatibilities() {
        //TODO: add a reload method from lexiconfig so we can actually use the fields above

        //---- Voice Chat Interaction ----\\
        if (Services.PLATFORM.isModLoaded("vcinteraction")) {
            CommonSimpleRadio.info("Voice Chat Interaction is present!");
            if (CommonSimpleRadio.SERVER_CONFIG.compatibilities.voice_chat_interaction.enabled) {
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
                if (CommonSimpleRadio.SERVER_CONFIG.compatibilities.vibrative_voice.enabled) {
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
            if (CommonSimpleRadio.SERVER_CONFIG.compatibilities.valkyrien_skies.enabled) {
                VALKYRIEN_SKIES = true;
                CommonSimpleRadio.info("..and compat is enabled!");
            } else {
                CommonSimpleRadio.info("..but compat is disabled");
            }
        }
    }

    public static void onData(RadioChannel channel, RadioSource source, short[] decoded) {
        // ---- Vibrative Voice ---- \\
        if (Services.PLATFORM.isModLoaded("vibrativevoice") && CommonSimpleRadio.SERVER_CONFIG.compatibilities.voice_chat_interaction.enabled) {
            VibrativeCompat.onData(channel, source, decoded);
        }
    }
}
