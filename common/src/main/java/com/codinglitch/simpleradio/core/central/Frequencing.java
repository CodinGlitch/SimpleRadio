package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface Frequencing {
    static boolean validate(WorldlyPosition position, Class<? extends Frequencing> clazz, @Nullable Frequency frequency) {
        BlockPos pos = position.realLocation();

        if (!position.level.isLoaded(pos)) return false;

        BlockState state = position.level.getBlockState(pos);
        if (state.isAir()) return false;

        BlockEntity blockEntity = position.level.getBlockEntity(pos);

        if (clazz.isInstance(state.getBlock().asItem()))
            return frequency == null || ((Frequencing) state.getBlock().asItem()).getFrequency(blockEntity) == frequency;
        return false;
    }
    static boolean validate(UUID uuid, Class<? extends Frequencing> clazz, @Nullable Frequency frequency) {
        VoicechatConnection connection = CommonRadioPlugin.serverApi.getConnectionOf(uuid);
        if (connection != null) return validate(connection, clazz, frequency);
        return false;
    }
    static boolean validate(VoicechatConnection connection, Class<? extends Frequencing> clazz, @Nullable Frequency frequency) {
        ServerPlayer player = (ServerPlayer) connection.getPlayer().getPlayer();
        if (player == null) return false;
        return validate(player, clazz, frequency);
    }
    static boolean validate(Entity entity, Class<? extends Frequencing> clazz, @Nullable Frequency frequency) { //TODO: find way to support entities
        return CommonRadioPlugin.verifyInventory(entity, stack -> {
            if (stack.getItem().getClass().isInstance(clazz))
                return frequency == null || ((Frequencing) stack.getItem()).getFrequency(stack) == frequency;
            return false;
        });
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
        if (blockEntity instanceof CentralBlockEntity frequencyBlockEntity)
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
        if (blockEntity instanceof CentralBlockEntity frequencyBlockEntity)
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
    default boolean validate(String frequency, Frequency.Modulation modulation, UUID owner) {
        if (frequency == null) return false;
        if (modulation == null) return false;
        return this.validate(Frequency.getOrCreateFrequency(frequency, modulation), owner);
    }
    default boolean validate(Frequency frequency, UUID owner) {
        RadioReceiver receiver = frequency.getReceiver(owner);
        return receiver != null;
    }

    default void tick(ItemStack stack, Level level) {
        if (level.isClientSide) return;
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
