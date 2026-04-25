package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.routers.Router;
import com.codinglitch.simpleradio.routers.Speaker;
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
    default Speaker startSpeaking(Entity owner, @Nullable UUID id) {
        return setupSpeaker(ServerSimpleRadioApi.getInstance().speakers().getOrCreate(owner, id));
    }
    /**
     * Start speaking in the world.
     * @param location the location to speak at
     * @param id the UUID of the speaker
     * @return The speaker created.
     */
    default Speaker startSpeaking(WorldlyPosition location, @Nullable UUID id) {
        return setupSpeaker(ServerSimpleRadioApi.getInstance().speakers().getOrCreate(location, id));
    }

    default Speaker setupSpeaker(Speaker speaker) {
        //RadioManager.registerSpeaker(speaker);

        return speaker;
    }

    /**
     * Stop speaking in the world.
     * This will remove the speaker from <b>both</b> global and specific router maps.
     * @param reference the UUID of the router that will stop speaking
     */
    default void stopSpeaking(UUID reference, boolean isClient) {
        if (isClient) {
            ClientSimpleRadioApi.getInstance().removeSpeaker(reference);
        } else {
            ServerSimpleRadioApi.getInstance().removeRouter(
                    ServerSimpleRadioApi.getInstance().speakers().remove(reference)
            );
        }
    }
}
