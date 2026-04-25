package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ClientSimpleRadioApi;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.routers.Listener;
import com.codinglitch.simpleradio.routers.Router;
import com.codinglitch.simpleradio.routers.Speaker;
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
     * This will remove the listener from <b>both</b> global and specific router maps.
     * @param reference the UUID of the router that will stop speaking
     */
    default void stopListening(UUID reference, boolean isClient) {
        if (isClient) {
            ClientSimpleRadioApi.getInstance().removeListener(reference);
        } else {
            ServerSimpleRadioApi.getInstance().removeRouter(
                    ServerSimpleRadioApi.getInstance().listeners().remove(reference)
            );
        }
    }
}
