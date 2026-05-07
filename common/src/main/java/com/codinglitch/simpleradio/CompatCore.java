package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.sable.CommonSableCompat;
import com.codinglitch.simpleradio.compat.CompatibilityInstance;
import com.codinglitch.simpleradio.compat.cc.CommonCCCompat;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSource;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.Source;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Optional;
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
            "CC:Tweaked", "computercraft", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.cc_tweaked
    );
    public static CompatibilityInstance ETCHED = new CompatibilityInstance(
            "Etched", "etched", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.etched
    );
    public static CompatibilityInstance AUDIO_PLAYER = new CompatibilityInstance(
            "AudioPlayer", "audioplayer", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.audioplayer
    );
    public static CompatibilityInstance SABLE = new CompatibilityInstance(
            "Sable", "sable", SimpleRadioLibrary.SERVER_CONFIG.compatibilities.sable
    );

    public static void postInitialize() {
        Services.COMPAT.postInitialize();
    }

    public static void spoutCompatibilities() {
        VC_INTERACTION.spout();
        VIBRATIVE_VOICE.spout();

        VALKYRIEN_SKIES.spout();
        CREATE.spout();
        SABLE.spout();
        COMPUTER_CRAFT.spout();

        ETCHED.spout();
        AUDIO_PLAYER.spout();

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
        }
    }

    public static void removeBlockEntity(BlockEntity blockEntity) {
        if (CompatCore.COMPUTER_CRAFT.isLoaded) {
            CommonCCCompat.removePeripheral(blockEntity);
        }
    }

    public static void acceptSource(Router router, Source source) {
        if (CompatCore.COMPUTER_CRAFT.isLoaded) {
            CommonCCCompat.acceptSource(router, source);
        }
    }

    public static String getSound(HolderLookup.Provider provider, ItemStack stack) {
        String result = Services.COMPAT.getSound(stack);
        if (result != null) return result;

        Optional<Holder<JukeboxSong>> song = JukeboxSong.fromStack(provider, stack);
        return song.map(s -> s.value().soundEvent().value().getLocation().toString()).orElse(null);
    }

    public static WorldlyPosition modifyPosition(WorldlyPosition position) {

        if (CompatCore.SABLE.enabled) {
            WorldlyPosition newPosition = CommonSableCompat.modifyPosition(position);
            if (newPosition != null) return newPosition;
        }

        return Services.COMPAT.modifyPosition(position);
    }

    public static Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {

        if (CompatCore.SABLE.enabled) {
            Quaternionf newRotation = CommonSableCompat.modifyRotation(position, rotation);
            if (newRotation != null) return newRotation;
        }

        return Services.COMPAT.modifyRotation(position, rotation);
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
