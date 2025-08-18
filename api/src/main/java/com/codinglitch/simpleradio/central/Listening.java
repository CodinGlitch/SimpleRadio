package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.routers.Listener;
import com.codinglitch.simpleradio.routers.Router;
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
    default Listener startListening(Entity owner, @Nullable UUID id) {
        return setupListener(ServerSimpleRadioApi.getInstance().listeners().getOrCreate(owner, id));
    }
    /**
     * Start listening in the world.
     * @param location the location to listen to
     * @param id the UUID of the listener
     * @return The listener created.
     */
    default Listener startListening(WorldlyPosition location, @Nullable UUID id) {
        return setupListener(ServerSimpleRadioApi.getInstance().listeners().getOrCreate(location, id));
    }

    default Listener setupListener(Listener listener) {
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
        Router router;
        if (isClient) {
            router = ClientSimpleRadioApi.getInstance().removeRouter(owner, "RadioListener");
        } else {
            router = ServerSimpleRadioApi.getInstance().listeners().remove(owner);
        }
        if (router != null) router.invalidate();
    }

    /**
     * Stop listening in the world.
     * @param location the location of the listener to remove
     */
    default void stopListening(WorldlyPosition location) {
        Router router;
        if (location.isClientSide()) {
            router = ClientSimpleRadioApi.getInstance().removeRouter(location, "RadioListener");
        } else {
            router = ServerSimpleRadioApi.getInstance().listeners().remove(location);
        }
        if (router != null) router.invalidate();
    }

    /**
     * Stop listening in the world. Infers information from itself. <b>Only call this on the server.</b>
     */
    default void stopListening() {
        if (this instanceof AuditoryBlockEntity blockEntity) {
            if (blockEntity.listener != null) {
                stopListening(blockEntity.listener.getLocation());
            }
        }
    }
}
