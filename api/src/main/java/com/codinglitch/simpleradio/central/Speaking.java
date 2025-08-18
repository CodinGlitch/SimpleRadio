package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
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
     * @param owner the Entity that will stop speaking
     * @param isClient if to remove in client
     */
    default void stopSpeaking(UUID owner, boolean isClient) {
        Router router;
        if (isClient) {
            router = ClientSimpleRadioApi.getInstance().removeRouter(owner, "RadioSpeaker");
        } else {
            router = ServerSimpleRadioApi.getInstance().listeners().remove(owner);
        }
        if (router != null) router.invalidate();
    }

    /**
     * Stop speaking in the world.
     * @param location the location of the speaker to remove
     */
    default void stopSpeaking(WorldlyPosition location) {
        Router router;
        if (location.isClientSide()) {
            router = ClientSimpleRadioApi.getInstance().removeRouter(location, "RadioSpeaker");
        } else {
            router = ServerSimpleRadioApi.getInstance().listeners().remove(location);
        }
        if (router != null) router.invalidate();
    }

    /**
     * Stop speaking in the world. Infers information from itself.
     */
    default void stopSpeaking() {
        if (this instanceof AuditoryBlockEntity blockEntity) {
            if (blockEntity.speaker != null) {
                stopSpeaking(blockEntity.speaker.getLocation());
            }
        }
    }
}
