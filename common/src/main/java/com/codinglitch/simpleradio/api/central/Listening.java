package com.codinglitch.simpleradio.api.central;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.registry.blocks.AuditoryBlockEntity;
import com.codinglitch.simpleradio.radio.RadioListener;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface Listening extends Auricular {

    /**
     * Start listening in the world.
     * @param owner the Entity that will listen
     * @param id the UUID of the listener
     * @return The listener created.
     */
    default RadioListener startListening(Entity owner, @Nullable UUID id) {
        return setupListener(RadioManager.getOrCreateListener(owner, id));
    }
    /**
     * Start listening in the world.
     * @param location the location to listen to
     * @param id the UUID of the listener
     * @return The listener created.
     */
    default RadioListener startListening(WorldlyPosition location, @Nullable UUID id) {
        return setupListener(RadioManager.getOrCreateListener(location, id));
    }

    default RadioListener setupListener(RadioListener listener) {
        if (this instanceof AuditoryBlockEntity blockEntity) {
            listener.transformer(source -> {
                source.delegate(blockEntity.id);

                return source;
            });
        }

        //RadioManager.registerListener(listener);

        return listener;
    }

    /**
     * Stop listening in the world.
     * @param owner the Entity that will stop listening
     * @param isClient if to remove in client
     */
    default void stopListening(UUID owner, boolean isClient) {
        if (isClient) {
            ClientRadioManager.removeRouter(owner);
        } else {
            RadioManager.removeListener(owner);
        }
    }

    /**
     * Stop listening in the world.
     * @param location the location of the listener to remove
     */
    default void stopListening(WorldlyPosition location) {
        if (location.isClientSide()) {
            ClientRadioManager.removeRouter(location);
        } else {
            RadioManager.removeListener(location);
        }
    }

    /**
     * Stop listening in the world. Infers information from itself. <b>Only call this on the server.</b>
     */
    default void stopListening() {
        if (this instanceof AuditoryBlockEntity blockEntity) {
            if (blockEntity.listener != null) {
                stopListening(blockEntity.listener.location);
                blockEntity.listener.invalidate();
            }
        }
    }
}
