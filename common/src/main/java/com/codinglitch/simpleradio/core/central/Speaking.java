package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.registry.blocks.AuditoryBlockEntity;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

public interface Speaking extends Auricular {
    /**
     * Start speaking in the world.
     * @param owner the Entity that will speak
     * @param id the UUID of the speaker
     * @return The speaker created.
     */
    default RadioSpeaker startSpeaking(Entity owner, @Nullable UUID id) {
        return setupSpeaker(RadioManager.getOrCreateSpeaker(owner, id));
    }
    /**
     * Start speaking in the world.
     * @param location the location to speak at
     * @param id the UUID of the speaker
     * @return The speaker created.
     */
    default RadioSpeaker startSpeaking(WorldlyPosition location, @Nullable UUID id) {
        return setupSpeaker(RadioManager.getOrCreateSpeaker(location, id));
    }

    default RadioSpeaker setupSpeaker(RadioSpeaker speaker) {
        //RadioManager.registerSpeaker(speaker);

        return speaker;
    }

    /**
     * Stop speaking in the world.
     * @param owner the Entity that will stop speaking
     */
    default void stopSpeaking(Entity owner) {
        if (owner.level().isClientSide) {
            ClientRadioManager.removeRouter(owner);
        } else {
            RadioManager.removeSpeaker(owner);
        }
    }

    /**
     * Stop speaking in the world.
     * @param location the location of the speaker to remove
     */
    default void stopSpeaking(WorldlyPosition location) {
        if (location.isClientSide()) {
            ClientRadioManager.removeRouter(location);
        } else {
            RadioManager.removeSpeaker(location);
        }
    }

    /**
     * Stop speaking in the world. Infers information from itself.
     */
    default void stopSpeaking() {
        if (this instanceof AuditoryBlockEntity blockEntity) {
            if (blockEntity.speaker != null) {
                stopSpeaking(blockEntity.speaker.location);
                blockEntity.speaker.invalidate();
            }
        }
    }
}
