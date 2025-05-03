package com.codinglitch.simpleradio.api.central;

import com.codinglitch.simpleradio.core.registry.blocks.*;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface Frequencing {
    static boolean validateLocation(WorldlyPosition position, Class<?> clazz, UUID reference, @Nullable Frequency frequency) {
        return RadioManager.getInstance().verifyLocationCollection(position, clazz);
    }

    /**
     * Mark this frequencing instance as dirty such that it will recalculate antenna strength at the next possible chance.
     */
    default void markDirty() {}

    /**
     * Get the current cached antenna power.
     * Only works if the instance has overriden this method.
     * Use this method instead of {@link Frequencing#calculateAntennaPower(BlockPos, Level)} whenever possible.
     * @return The antenna power
     */
    default int getAntennaPower() { return 0; }

    /**
     * Calculate the power of the attached antenna (via climbing).
     * Use sparingly, and use {@link Frequencing#getAntennaPower()} instead whenever possible.
     * @param corePosition the location of the core block
     * @param level the Level to check
     * @return The power of the connected antenna.
     */
    default int calculateAntennaPower(BlockPos corePosition, Level level) {
        BlockPos basePosition = this.getAntennaBase(corePosition, level);
        BlockState state = level.getBlockState(basePosition);

        if (state.getBlock() instanceof AntennaBlock antennaBlock) {
            return antennaBlock.climbAntenna(basePosition, level);
        }

        return 0;
    }

    /**
     * Get the location of the base of the antenna connected to this core block.
     * @param pos the location of the core block
     * @param level the Level to check
     * @return The position of the base of the antenna.
     */
    default BlockPos getAntennaBase(BlockPos pos, Level level) {
        BlockPos travelledPosition = InsulatorBlock.travelExtension(pos, level);
        if (travelledPosition != pos) return travelledPosition.above();

        return pos.above();
    }


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
     * Sets the frequency for a BlockEntity.
     * @param blockEntity the BlockEntity to change the frequency of
     * @param frequencyName the frequency to set it to
     * @param modulation the modulation type of the frequency
     */
    default void setFrequency(BlockEntity blockEntity, String frequencyName, Frequency.Modulation modulation) {
        setFrequency(blockEntity, Frequency.getOrCreateFrequency(frequencyName, modulation));
    }
    /**
     * Sets the frequency for a BlockEntity.
     * @param blockEntity the BlockEntity to change the frequency of
     * @param frequency the frequency to set it to
     */
    default void setFrequency(BlockEntity blockEntity, Frequency frequency) {
        if (blockEntity instanceof AuditoryBlockEntity frequencyBlockEntity)
            frequencyBlockEntity.frequency = frequency;
    }

    /**
     * Gets the frequency for an ItemStack.
     * @param stack the ItemStack to get the frequency of
     * @return The frequency, or null if it doesn't have one.
     */
    default Frequency getFrequency(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains("frequency") || !tag.contains("modulation")) return null;

        String frequencyName = tag.getString("frequency");
        Frequency.Modulation modulation = Frequency.modulationOf(tag.getString("modulation"));
        return Frequency.getOrCreateFrequency(frequencyName, modulation);
    }
    /**
     * Gets the frequency for a BlockEntity.
     * @param blockEntity the BlockEntity to get the frequency of
     * @return The frequency, or null if it doesn't have one.
     */
    default Frequency getFrequency(BlockEntity blockEntity) {
        if (blockEntity instanceof AuditoryBlockEntity frequencyBlockEntity)
            return frequencyBlockEntity.frequency;
        return null;
    }

    default String getDefaultFrequency() {
        return Frequency.DEFAULT_FREQUENCY;
    }

    default Frequency.Modulation getDefaultModulation() {
        return Frequency.DEFAULT_MODULATION;
    }

    /**
     * Validates whether a UUID is present in the frequency.
     * @param frequency the frequency to check
     * @param modulation the modulation type of the frequency
     * @param owner the UUID to validate
     * @return Whether it is present in the frequency.
     */
    default boolean validateLocation(String frequency, Frequency.Modulation modulation, UUID owner) {
        if (frequency == null) return false;
        if (modulation == null) return false;
        return this.validateLocation(Frequency.getOrCreateFrequency(frequency, modulation), owner);
    }
    default boolean validateLocation(Frequency frequency, UUID owner) {
        RadioReceiver receiver = frequency.getReceiver(owner);
        return receiver != null;
    }

    default void tick(ItemStack stack, Level level) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("frequency") || tag.getString("frequency").isEmpty())
            setFrequency(stack, this.getDefaultFrequency(), this.getDefaultModulation());
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
