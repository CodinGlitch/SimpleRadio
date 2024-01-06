package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.RadioChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public interface Transceiving {
    Random RANDOM = new Random();

    default CompoundTag setFrequency(ItemStack stack, String frequencyName, Frequency.Modulation modulation) {
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString("frequency", frequencyName);
        tag.putString("modulation", modulation.shorthand);

        return tag;
    }

    default RadioChannel listen(String frequencyName, Frequency.Modulation modulation, UUID owner) {
        Frequency frequency = Frequency.getOrCreateFrequency(frequencyName, modulation);
        return frequency.tryAddListener(owner);
    }

    default void stopListening(String frequencyName, Frequency.Modulation modulation, UUID owner) {
        Frequency frequency = Frequency.getOrCreateFrequency(frequencyName, modulation);
        frequency.removeListener(owner);
    }

    default void tick(ItemStack stack, Level level, Entity entity) {
        if (level.isClientSide) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("frequency") || tag.getString("frequency").isEmpty())
            setFrequency(stack,
                    "000.00",
                    Frequency.Modulation.FREQUENCY
            );
    }

    default void appendTooltip(ItemStack stack, List<Component> components) {
        CompoundTag tag = stack.getOrCreateTag();

        components.add(Component.literal(
                tag.getString("frequency") + tag.getString("modulation")
        ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
