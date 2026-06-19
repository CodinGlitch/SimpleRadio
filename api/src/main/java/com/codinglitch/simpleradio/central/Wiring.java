package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.radio.Message;
import com.codinglitch.simpleradio.routers.Router;
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
     * Get the router opposite to the one provided.
     * @param source The originating socket
     */
    Router transport(Socket source);

    /**
     * Get the router opposite to the one provided.
     * @param reference The originating UUID
     */
    Router transport(UUID reference);

    /**
     * Relay a {@link Message} along this wire.
     * @param source The {@link Message} to relay
     * @param originSocket The {@link Socket} the source came from
     */
    void relay(Message source, Socket originSocket);

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

    void burnOut();

    void shortCircuit(Vector3f at);
    void shortCircuit();

    void queueDemise(int time, float position);

    void cleanUp();
}
