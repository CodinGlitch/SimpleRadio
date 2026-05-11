package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.routers.Receiver;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.codinglitch.simpleradio.core.SimpleRadioComponents.*;

public interface Frequencing {
    static boolean validateLocation(WorldlyPosition position, Class<?> clazz, UUID reference, @Nullable Frequency frequency) {
        return ServerSimpleRadioApi.getInstance().verifyLocationCollection(position, clazz);
    }

    /**
     * Mark this frequencing instance as dirty such that it will recalculate antenna strength at the next possible chance.
     */
    default void markDirty() {}

    /**
     * Get the current cached antenna power.
     * Only works if the instance has overridden this method.
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

        if (state.getBlock() instanceof Antennal antennal) {
            return antennal.climbAntenna(basePosition, level);
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
        BlockPos travelledPosition = ServerSimpleRadioApi.getInstance().travelExtension(pos, level);
        if (travelledPosition != pos) return travelledPosition.above();

        return pos.above();
    }


    /**
     * Sets the frequency for an ItemStack.
     * @param stack the ItemStack to change the frequency of
     * @param frequencyName the frequency to set it to
     * @param modulation the modulation type of the frequency
     */
    default void setFrequency(ItemStack stack, String frequencyName, Frequency.Modulation modulation) {
        stack.set(FREQUENCY, frequencyName);
        stack.set(MODULATION, modulation);
    }
    /**
     * Sets the frequency for a BlockEntity.
     * @param blockEntity the BlockEntity to change the frequency of
     * @param frequencyName the frequency to set it to
     * @param modulation the modulation type of the frequency
     */
    default void setFrequency(BlockEntity blockEntity, String frequencyName, Frequency.Modulation modulation) {
        setFrequency(blockEntity, SimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation));
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
        if (!stack.has(FREQUENCY) || !stack.has(MODULATION)) return null;

        String frequencyName = stack.get(FREQUENCY);
        Frequency.Modulation modulation = stack.get(MODULATION);
        return SimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation);
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
        return SimpleRadioApi.getInstance().frequencies().defaultFrequency();
    }

    default Frequency.Modulation getDefaultModulation() {
        return SimpleRadioApi.getInstance().frequencies().defaultModulation();
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
        return this.validateLocation(SimpleRadioApi.getInstance().frequencies().getOrCreate(frequency, modulation), owner);
    }
    default boolean validateLocation(Frequency frequency, UUID owner) {
        Receiver receiver = frequency.getReceiver(owner);
        return receiver != null;
    }

    default void tick(ItemStack stack, Level level) {
        if (!stack.has(FREQUENCY) || stack.get(FREQUENCY).isEmpty())
            setFrequency(stack, this.getDefaultFrequency(), this.getDefaultModulation());
    }

    default void appendTooltip(ItemStack stack, List<Component> components) {
        if (stack.has(FREQUENCY)) {
            components.add(Component.literal(
                    stack.get(FREQUENCY) + stack.get(MODULATION).shorthand
            ).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (Screen.hasShiftDown() && stack.has(REFERENCE)) {
            components.add(Component.translatable(
                    "tooltip.simpleradio.receiver_user",
                    stack.get(REFERENCE).toString()
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
