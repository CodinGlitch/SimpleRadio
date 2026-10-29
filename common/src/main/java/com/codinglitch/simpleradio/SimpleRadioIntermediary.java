package com.codinglitch.simpleradio;

import com.codinglitch.intermediator.Intermediary;
import com.codinglitch.intermediator.Intermediation;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.CommonAudioPlayerCompat;
import com.codinglitch.simpleradio.compat.phases.ComputeSoundPhase;
import com.codinglitch.simpleradio.compat.phases.ModifyPhase;
import com.codinglitch.simpleradio.compat.phases.RetrieveAccessoryPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import org.joml.Quaternionf;

import java.util.List;
import java.util.function.Predicate;

import static com.codinglitch.simpleradio.SimpleRadioLibrary.SERVER_CONFIG;

public class SimpleRadioIntermediary extends Intermediary {
    public static SimpleRadioIntermediary INSTANCE;
    public SimpleRadioIntermediary() { INSTANCE = this; }

    public static Intermediation VC_INTERACTION = new Intermediation(
            "Voice Chat Interaction", "vcinteraction", SERVER_CONFIG.compatibilities.voice_chat_interaction,
            () -> List.of(SimpleRadioIntermediary.VIBRATIVE_VOICE)
    );
    public static Intermediation VIBRATIVE_VOICE = new Intermediation(
            "Vibrative Voice", "vibrativevoice", SERVER_CONFIG.compatibilities.vibrative_voice,
            () -> List.of(SimpleRadioIntermediary.VC_INTERACTION)
    );
    public static Intermediation VALKYRIEN_SKIES = new Intermediation(
            "Valkyrien Skies", "valkyrienskies", SERVER_CONFIG.compatibilities.valkyrien_skies
    );
    public static Intermediation CREATE = new Intermediation(
            "Create", "create", SERVER_CONFIG.compatibilities.create,
            "[6.0,)"
    );
    public static Intermediation COMPUTER_CRAFT = new Intermediation(
            "CC:Tweaked", "computercraft", SERVER_CONFIG.compatibilities.cc_tweaked
    );
    public static Intermediation ETCHED = new Intermediation(
            "Etched", "etched", SERVER_CONFIG.compatibilities.etched
    );
    public static Intermediation AUDIO_PLAYER = new Intermediation(
            "AudioPlayer", "audioplayer", SERVER_CONFIG.compatibilities.audioplayer
    );
    public static Intermediation SABLE = new Intermediation(
            "Sable", "sable", SERVER_CONFIG.compatibilities.sable
    );

    public static Intermediation CURIOS = new Intermediation(
            "Curios", "curios", SERVER_CONFIG.compatibilities.curios
    );
    public static Intermediation TRINKETS = new Intermediation(
            "Trinkets", "trinkets", SERVER_CONFIG.compatibilities.trinkets
    );

    public static WorldlyPosition modifyPosition(WorldlyPosition position) {
        return SimpleRadioIntermediary.INSTANCE.post(new ModifyPhase.Position(position)).position;
    }

    public static Quaternionf modifyRotation(WorldlyPosition position, Quaternionf rotation) {
        return SimpleRadioIntermediary.INSTANCE.post(new ModifyPhase.Rotation(position, rotation)).rotation;
    }

    public static String getSound(ItemStack stack) {
        ComputeSoundPhase result = SimpleRadioIntermediary.INSTANCE.post(new ComputeSoundPhase(stack));
        if (result.sound != null && !result.sound.isEmpty()) return result.sound;

        if (stack.getItem() instanceof RecordItem recordItem) {
            return recordItem.getSound().getLocation().toString();
        }

        return null;
    }

    public static ItemStack getAccessory(Player player, Predicate<ItemStack> filter) {
        return SimpleRadioIntermediary.INSTANCE.post(new RetrieveAccessoryPhase(player, filter)).stack;
    }
    
    @Override
    public void intermediate() {
        begin()
                .add(AUDIO_PLAYER, () -> CommonAudioPlayerCompat::new)
                .mediate();
    }

    @Override
    public void postInitialize() {

    }
}
