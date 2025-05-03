package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.radio.RadioRouter;
import com.codinglitch.simpleradio.radio.RadioSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public interface Wiring extends Medium {

    /**
     * Get the router opposite to the one provided.
     * @param source The originating router
     */
    RadioRouter transport(RadioRouter source);

    /**
     * Relay a {@link RadioSource} along this wire.
     * @param source The {@link RadioSource} to relay
     * @param originSocket The {@link Socket} the source came from
     */
    void relay(RadioSource source, Socket originSocket);

    float getLength();

    @Nullable
    RadioRouter getFromRouter();
    Optional<UUID> getFrom();

    @Nullable
    String getFromType();
    void setFrom(RadioRouter from);

    @Nullable
    RadioRouter getToRouter();
    Optional<UUID> getTo();

    @Nullable
    String getToType();
    void setTo(RadioRouter to);

    void burnOut();

    void shortCircuit(Vector3f at);
    void shortCircuit();

    void queueDemise(int time, float position);

    void cleanUp();
}
