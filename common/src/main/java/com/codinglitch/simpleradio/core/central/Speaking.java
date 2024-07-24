package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.RadioSource;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

public interface Speaking extends Auditory {
    /**
     * Start speaking in the world.
     * @param owner the Entity that will speak
     * @param id the UUID of the speaker
     * @return The speaker created.
     */
    default RadioSpeaker startSpeaking(Entity owner, @Nullable UUID id) {
        return setupSpeaker(RadioSpeaker.getOrCreateSpeaker(owner, id));
    }
    /**
     * Start speaker in the world.
     * @param location the location to speak at
     * @param id the UUID of the speaker
     * @return The speaker created.
     */
    default RadioSpeaker startSpeaking(WorldlyPosition location, @Nullable UUID id) {
        return setupSpeaker(RadioSpeaker.getOrCreateSpeaker(location, id));
    }

    default RadioSpeaker setupSpeaker(RadioSpeaker speaker) {
        if (this instanceof CentralBlockEntity blockEntity) {
            speaker.range = 12;
        }

        return speaker;
    }

    /**
     * Stop speaking in the world.
     * @param owner the Entity that will stop speaking
     */
    default void stopSpeaking(Entity owner) {
        RadioSpeaker.removeSpeaker(owner);
    }

    /**
     * Stop speaking in the world.
     * @param location the location of the speaker to remove
     */
    default void stopSpeaking(WorldlyPosition location) {
        RadioSpeaker.removeSpeaker(location);
    }

    /**
     * Stop speaking in the world. Infers information from itself.
     */
    default void stopSpeaking() {
        if (this instanceof CentralBlockEntity blockEntity) {
            if (blockEntity.speaker != null) {
                stopSpeaking(blockEntity.speaker.location);
                blockEntity.speaker.invalidate();
            }
        }
    }
}
