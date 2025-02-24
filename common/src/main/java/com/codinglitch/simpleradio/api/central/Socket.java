package com.codinglitch.simpleradio.api.central;

import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.radio.*;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A wire-connecting object.
 */
public interface Socket {
    default boolean canConnect() {
        return true;
    }
    default boolean canConnectTo(Socket other) {
        return true;
    }

    /**
     * Distribute a {@link RadioSource} along every wire connected to this socket.
     * @param source The {@link RadioSource} to distribute
     * @return Whether or not the source was distributed across any wires.
     */
    default boolean distribute(RadioSource source) {
        boolean result = false;

        ArrayList<Wire> wires = this.getWires();
        wires.removeIf(Predicate.not(Wire::isAlive));

        for (int i = 0; i < wires.size(); i++) {
            Wire wire = wires.get(i);
            if (source.wireMedium != null && source.wireMedium.getUUID().equals(wire.getUUID())) continue;

            RadioSource oldSource = source;
            if (i < wires.size()-1) source = source.copy();

            wire.relay(oldSource, this);

            result = true;
        }

        return result;
    }

    default boolean hasWire(Wire wire) {
        UUID from = wire.getFrom().orElse(null);
        UUID to = wire.getTo().orElse(null);
        if (from == null || to == null) return false;

        for (Wire otherWire : this.getWires()) {
            UUID otherFrom = otherWire.getFrom().orElse(null);
            UUID otherTo = otherWire.getTo().orElse(null);
            if (otherFrom == null || otherTo == null) continue;

            if (otherFrom.equals(from) && otherTo.equals(to)) return true;
            if (otherFrom.equals(to) && otherTo.equals(from)) return true;
        }

        return false;
    }

    default void connect(Wire wire) {
        this.getWires().add(wire);
    }
    default void disconnect(Wire wire) {
        this.getWires().removeIf(otherWire -> otherWire.equals(wire));
    }
    default void disconnect(UUID wire) {
        this.getWires().removeIf(otherWire -> otherWire.getUUID().equals(wire));
    }

    default UUID getID() {
        return this.getRouter().getID();
    }

    default ArrayList<Wire> getWires() {
        return this.getRouter().getWires();
    }

    default void shortCircuit() {
        for (Object wire : this.getWires().toArray()) {
            ((Wire) wire).kill();
        }
        this.getWires().clear();
    }

    /**
     * Override this to <i>expose</i> a given router to wires. <br>
     * Allows blocks with more than one router to choose which of their routers wires will connect to.
     * @return The router to be exposed
     */
    RadioRouter getRouter();
}
