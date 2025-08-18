package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.routers.Transmitter;
import net.minecraft.world.entity.Entity;

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
    default Transmitter startTransmitting(WorldlyPosition location, String frequencyName, Frequency.Modulation modulation, UUID id) {
        return startTransmitting(location, ServerSimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation), id);
    }
    default Transmitter startTransmitting(WorldlyPosition location, Frequency frequency, UUID id) {
        return frequency.tryAddTransmitter(id, location);
    }
    default Transmitter startTransmitting(WorldlyPosition location, Frequency frequency) {
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
    default Transmitter startTransmitting(Entity entity, String frequencyName, Frequency.Modulation modulation, UUID id) {
        return startTransmitting(entity, ServerSimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation), id);
    }
    default Transmitter startTransmitting(Entity entity, Frequency frequency, UUID id) {
        return frequency.tryAddTransmitter(id, entity);
    }
    default Transmitter startTransmitting(Entity entity, Frequency frequency) {
        return startTransmitting(entity, frequency, UUID.randomUUID());
    }

    /**
     * Stop listening in a certain frequency
     * @param frequencyName the frequency to stop listening to
     * @param modulation the modulation type of the frequency
     * @param owner the UUID to remove
     */
    default void stopTransmitting(String frequencyName, Frequency.Modulation modulation, UUID owner) {
        Frequency frequency = ServerSimpleRadioApi.getInstance().frequencies().get(frequencyName, modulation);
        if (frequency != null) {
            frequency.removeTransmitter(owner);
        }
    }

    /**
     * Stop receiving. Infers information from itself.
     */
    /*default void stopTransmitting() {
        if (this instanceof AuditoryBlockEntity blockEntity) {
            if (blockEntity.transmitter != null && blockEntity.transmitter.frequency != null) {
                stopTransmitting(blockEntity.transmitter.frequency.frequency, blockEntity.transmitter.frequency.modulation, blockEntity.id);
                blockEntity.transmitter.invalidate();
            }
        }
    }*/
}
