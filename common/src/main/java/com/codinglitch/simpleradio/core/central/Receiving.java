package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioTransmitter;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
    default RadioReceiver startReceiving(WorldlyPosition location, String frequencyName, Frequency.Modulation modulation, UUID id) {
        return startReceiving(location, Frequency.getOrCreateFrequency(frequencyName, modulation), id);
    }
    default RadioReceiver startReceiving(WorldlyPosition location, Frequency frequency) {
        return startReceiving(location, frequency, UUID.randomUUID());
    }
    default RadioReceiver startReceiving(WorldlyPosition location, Frequency frequency, UUID id) {
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
    default RadioReceiver startReceiving(Entity entity, String frequencyName, Frequency.Modulation modulation, UUID id) {
        return startReceiving(entity, Frequency.getOrCreateFrequency(frequencyName, modulation), id);
    }
    default RadioReceiver startReceiving(Entity entity, Frequency frequency) {
        return startReceiving(entity, frequency, UUID.randomUUID());
    }
    default RadioReceiver startReceiving(Entity entity, Frequency frequency, UUID id) {
        return frequency.tryAddReceiver(id, entity);
    }


    /**
     * Stop listening in a certain frequency
     * @param frequencyName the frequency to stop listening to
     * @param modulation the modulation type of the frequency
     * @param owner the UUID to remove
     */
    default void stopReceiving(String frequencyName, Frequency.Modulation modulation, UUID owner) {
        Frequency frequency = Frequency.getFrequency(frequencyName, modulation);
        if (frequency != null) {
            frequency.removeReceiver(owner);
        }
    }

    /**
     * Stop receiving. Infers information from itself.
     */
    default void stopReceiving() {
        if (this instanceof CentralBlockEntity blockEntity) {
            if (blockEntity.receiver != null && blockEntity.receiver.frequency != null) {
                stopReceiving(blockEntity.receiver.frequency.frequency, blockEntity.receiver.frequency.modulation, blockEntity.id);
                blockEntity.receiver.invalidate();
            }
        }
    }
}
