package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
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
        return startTransmitting(location, SimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation), id);
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
        return startTransmitting(entity, SimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation), id);
    }
    default Transmitter startTransmitting(Entity entity, Frequency frequency, UUID id) {
        return frequency.tryAddTransmitter(id, entity);
    }
    default Transmitter startTransmitting(Entity entity, Frequency frequency) {
        return startTransmitting(entity, frequency, UUID.randomUUID());
    }

    /**
     * Stop listening in a certain frequency.
     * This will remove the transmitter from <b>both</b> global and specific router maps.
     * @param frequencyName the frequency to stop listening to
     * @param modulation the modulation type of the frequency
     * @param reference the UUID to remove
     */
    default void stopTransmitting(String frequencyName, Frequency.Modulation modulation, UUID reference, boolean isClient) {
        Frequency frequency = SimpleRadioApi.getInstance(isClient).frequencies().get(frequencyName, modulation);
        if (frequency != null)
            SimpleRadioApi.removeRouterSided(frequency.removeTransmitter(reference), isClient);
    }
}
