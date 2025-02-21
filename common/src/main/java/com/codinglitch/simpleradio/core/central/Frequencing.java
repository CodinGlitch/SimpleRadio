package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.SimpleRadioCatalysts;
import com.codinglitch.simpleradio.core.registry.blocks.*;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.radio.RadioRouter;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface Frequencing {
    static boolean validate(WorldlyPosition position, Class<? extends Frequencing> clazz, @Nullable Frequency frequency) {
        return RadioManager.verifyLocationCollection(position, clazz);
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
    static boolean validate(Entity entity, Class<? extends Frequencing> clazz, @Nullable Frequency frequency) {
        return RadioManager.verifyEntityCollection(entity, stack -> {
            if (stack.getItem().getClass().isInstance(clazz))
                return frequency == null || ((Frequencing) stack.getItem()).getFrequency(stack) == frequency;
            return false;
        });
    }

    int getAntennaPower(WorldlyPosition corePosition);
    default int calculateAntennaPower(WorldlyPosition corePosition) {
        Level level = corePosition.level;

        BlockPos basePosition = this.getAntennaBase(corePosition).blockPos();
        BlockState state = level.getBlockState(basePosition);

        if (state.getBlock() instanceof AntennaBlock antennaBlock) {
            return antennaBlock.climbAntenna(basePosition, level);
        }

        return 0;
    }

    /**
     * Get the location of the base of the antenna connected to this core block.
     * @param location the location of the core block
     * @return The frequency, or null if it doesn't have one.
     */
    default WorldlyPosition getAntennaBase(WorldlyPosition location) {
        BlockPos coreBlockPos = location.blockPos();
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = coreBlockPos.relative(direction);
            BlockEntity blockEntity = location.level.getBlockEntity(offsetPos);

            if (blockEntity instanceof SocketBlockEntity socketBlockEntity) {
                List<Wire> wires = socketBlockEntity.getWires();
                if (wires.isEmpty()) continue;

                Wire wire = wires.get(0);
                RadioRouter router = wire.transport(socketBlockEntity.getRouter());
                BlockPos routerPos = router.location.blockPos();

                BlockState blockState = location.level.getBlockState(routerPos);
                if (!(blockState.getBlock() instanceof SocketBlock)) continue;

                Direction routerDirection = blockState.getValue(SocketBlock.FACING);
                return WorldlyPosition.of(routerPos.relative(routerDirection.getOpposite()).above(), router.location.level);
            }
        }

        return WorldlyPosition.of(coreBlockPos.above(), location.level);
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
