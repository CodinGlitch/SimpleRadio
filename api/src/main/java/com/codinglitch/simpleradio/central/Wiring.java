package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.radio.Source;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public interface Wiring extends Medium {

    /**
     * Get the router opposite to the one provided.
     * @param source The originating router
     */
    Router transport(Router source);

    /**
     * Relay a {@link Source} along this wire.
     * @param source The {@link Source} to relay
     * @param originSocket The {@link Socket} the source came from
     */
    void relay(Source source, Socket originSocket);

    float getLength();

    UUID getReference();

    @Nullable
    Router getFromRouter();
    Optional<UUID> getFrom();

    @Nullable
    String getFromType();
    void setFrom(Router from);

    @Nullable
    Router getToRouter();
    Optional<UUID> getTo();

    @Nullable
    String getToType();
    void setTo(Router to);

    boolean isValid();

    void burnOut(Entity.RemovalReason reason);
    void burnOut();

    void shortCircuit(Vector3f at);
    void shortCircuit();

    void queueDemise(int time, float position);

    void cleanUp();
}
