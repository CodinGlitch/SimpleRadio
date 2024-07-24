package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.*;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Transmitting extends Frequencing {

    /**
     * Start transmitting in a certain frequency.
     * @param location the location to transmit from
     * @param frequencyName the frequency to listen to
     * @param modulation the modulation type of the frequency
     * @param id the UUID that will listen
     * @return The channel created from the listener.
     */
    default RadioTransmitter startTransmitting(WorldlyPosition location, String frequencyName, Frequency.Modulation modulation, UUID id) {
        return startTransmitting(location, Frequency.getOrCreateFrequency(frequencyName, modulation), id);
    }
    default RadioTransmitter startTransmitting(WorldlyPosition location, Frequency frequency, UUID id) {
        return frequency.tryAddTransmitter(id, location);
    }
    default RadioTransmitter startTransmitting(WorldlyPosition location, Frequency frequency) {
        return startTransmitting(location, frequency, UUID.randomUUID());
    }

    /**
     * Start transmitting in a certain frequency.
     * @param entity the Entity to transmit from
     * @param frequencyName the frequency to listen to
     * @param modulation the modulation type of the frequency
     * @param id the UUID that will listen
     * @return The channel created from the listener.
     */
    default RadioTransmitter startTransmitting(Entity entity, String frequencyName, Frequency.Modulation modulation, UUID id) {
        return startTransmitting(entity, Frequency.getOrCreateFrequency(frequencyName, modulation), id);
    }
    default RadioTransmitter startTransmitting(Entity entity, Frequency frequency, UUID id) {
        return frequency.tryAddTransmitter(id, entity);
    }
    default RadioTransmitter startTransmitting(Entity entity, Frequency frequency) {
        return startTransmitting(entity, frequency, UUID.randomUUID());
    }

    /**
     * Stop listening in a certain frequency
     * @param frequencyName the frequency to stop listening to
     * @param modulation the modulation type of the frequency
     * @param owner the UUID to remove
     */
    default void stopTransmitting(String frequencyName, Frequency.Modulation modulation, UUID owner) {
        Frequency frequency = Frequency.getFrequency(frequencyName, modulation);
        if (frequency != null) {
            frequency.removeTransmitter(owner);
        }
    }

    /**
     * Stop receiving. Infers information from itself.
     */
    default void stopTransmitting() {
        if (this instanceof CentralBlockEntity blockEntity) {
            if (blockEntity.transmitter != null && blockEntity.transmitter.frequency != null) {
                stopTransmitting(blockEntity.transmitter.frequency.frequency, blockEntity.transmitter.frequency.modulation, blockEntity.id);
                blockEntity.transmitter.invalidate();
            }
        }
    }
}
