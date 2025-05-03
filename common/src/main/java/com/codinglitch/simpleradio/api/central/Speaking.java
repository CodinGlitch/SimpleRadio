package com.codinglitch.simpleradio.api.central;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.registry.blocks.AuditoryBlockEntity;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface Speaking extends Auricular {
    /**
     * Start speaking in the world.
     * @param owner the Entity that will speak
     * @param id the UUID of the speaker
     * @return The speaker created.
     */
    default RadioSpeaker startSpeaking(Entity owner, @Nullable UUID id) {
        return setupSpeaker(RadioManager.getInstance().getOrCreateSpeaker(owner, id));
    }
    /**
     * Start speaking in the world.
     * @param location the location to speak at
     * @param id the UUID of the speaker
     * @return The speaker created.
     */
    default RadioSpeaker startSpeaking(WorldlyPosition location, @Nullable UUID id) {
        return setupSpeaker(RadioManager.getInstance().getOrCreateSpeaker(location, id));
    }

    default RadioSpeaker setupSpeaker(RadioSpeaker speaker) {
        //RadioManager.registerSpeaker(speaker);

        return speaker;
    }

    /**
     * Stop speaking in the world.
     * @param owner the Entity that will stop speaking
     * @param isClient if to remove in client
     */
    default void stopSpeaking(UUID owner, boolean isClient) {
        if (isClient) {
            ClientRadioManager.removeRouter(owner);
        } else {
            RadioManager.getInstance().removeSpeaker(owner);
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
            RadioManager.getInstance().removeSpeaker(location);
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
