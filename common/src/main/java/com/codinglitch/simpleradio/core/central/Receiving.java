package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.RadioChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public interface Receiving {
    Random RANDOM = new Random();

    /**
     * Sets the frequency for an ItemStack.
     * @param stack the ItemStack to change the frequency of
     * @param frequencyName the frequency to set it to
     * @param modulation the modulation type of the frequency
     * @return The updated tag.
     */
    default CompoundTag setFrequency(ItemStack stack, String frequencyName, Frequency.Modulation modulation) {
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString("frequency", frequencyName);
        tag.putString("modulation", modulation.shorthand);

        return tag;
    }

    /**
     * Validates whether a UUID is present in the frequency.
     * @param frequency the frequency to check
     * @param modulation the modulation type of the frequency
     * @param owner the UUID to validate
     * @return Whether it is present in the frequency.
     */
    default boolean validate(String frequency, Frequency.Modulation modulation, UUID owner) {
        if (frequency == null) return false;
        if (modulation == null) return false;
        return this.validate(Frequency.getOrCreateFrequency(frequency, modulation), owner);
    }
    default boolean validate(Frequency frequency, UUID owner) {
        return frequency.getChannel(owner) != null;
    }

    /**
     * Start listening in a certain frequency.
     * @param frequencyName the frequency to listen to
     * @param modulation the modulation type of the frequency
     * @param owner the UUID that will listen
     * @return The channel created from the listener.
     */
    default RadioChannel listen(String frequencyName, Frequency.Modulation modulation, UUID owner) {
        Frequency frequency = Frequency.getOrCreateFrequency(frequencyName, modulation);
        return frequency.tryAddListener(owner);
    }

    /**
     * Stop listening in a certain frequency
     * @param frequencyName the frequency to stop listening to
     * @param modulation the modulation type of the frequency
     * @param owner the UUID to remove
     */
    default void stopListening(String frequencyName, Frequency.Modulation modulation, UUID owner) {
        Frequency frequency = Frequency.getOrCreateFrequency(frequencyName, modulation);
        frequency.removeListener(owner);
    }

    default void tick(ItemStack stack, Level level, Entity entity) {
        if (level.isClientSide) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("frequency") || tag.getString("frequency").isEmpty())
            setFrequency(stack, Frequency.DEFAULT_FREQUENCY, Frequency.DEFAULT_MODULATION);
    }

    default void appendTooltip(ItemStack stack, List<Component> components) {
        CompoundTag tag = stack.getOrCreateTag();

        if (tag.contains("frequency")) {
            components.add(Component.literal(
                    tag.getString("frequency") + tag.getString("modulation")
            ).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (Screen.hasShiftDown() && tag.contains("user")) {
            components.add(Component.translatable(
                    "tooltip.simpleradio.receiver_user",
                    tag.getUUID("user")
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
