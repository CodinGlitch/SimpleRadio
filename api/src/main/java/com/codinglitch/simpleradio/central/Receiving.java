package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.routers.Receiver;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public interface Receiving extends Frequencing {

    /**
     * Start receiving in a certain frequency.
     * @param location the location to receive at
     * @param frequencyName the frequency to listen to
     * @param modulation the modulation type of the frequency
     * @param id the UUID that will listen
     * @return The channel created from the listener.
     */
    default Receiver startReceiving(WorldlyPosition location, String frequencyName, Frequency.Modulation modulation, UUID id) {
        return startReceiving(location, SimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation), id);
    }
    default Receiver startReceiving(WorldlyPosition location, Frequency frequency) {
        return startReceiving(location, frequency, UUID.randomUUID());
    }
    default Receiver startReceiving(WorldlyPosition location, Frequency frequency, UUID id) {
        return frequency.tryAddReceiver(id, location);
    }

    /**
     * Start receiving in a certain frequency.
     * @param entity the Entity to receive at
     * @param frequencyName the frequency to listen to
     * @param modulation the modulation type of the frequency
     * @param id the UUID that will listen
     * @return The channel created from the listener.
     */
    default Receiver startReceiving(Entity entity, String frequencyName, Frequency.Modulation modulation, UUID id) {
        return startReceiving(entity, SimpleRadioApi.getInstance().frequencies().getOrCreate(frequencyName, modulation), id);
    }
    default Receiver startReceiving(Entity entity, Frequency frequency) {
        return startReceiving(entity, frequency, UUID.randomUUID());
    }
    default Receiver startReceiving(Entity entity, Frequency frequency, UUID id) {
        return frequency.tryAddReceiver(id, entity);
    }


    /**
     * Stop listening in a certain frequency.
     * This will remove the receiver from <b>both</b> global and specific router maps.
     * @param frequencyName the frequency to stop listening to
     * @param modulation the modulation type of the frequency
     * @param reference the UUID to remove
     */
    default void stopReceiving(String frequencyName, Frequency.Modulation modulation, UUID reference, boolean isClient) {
        Frequency frequency = SimpleRadioApi.getInstance(isClient).frequencies().get(frequencyName, modulation);
        if (frequency != null)
            SimpleRadioApi.removeRouterSided(frequency.removeReceiver(reference), isClient);
    }
}
