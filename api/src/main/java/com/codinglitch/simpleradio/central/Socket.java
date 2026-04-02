package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.radio.Source;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;
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
     * Distribute a {@link Source} along every wire connected to this socket.
     * @param source The {@link Source} to distribute
     * @return Whether or not the source was distributed across any wires.
     */
    default boolean distribute(Source source) {
        boolean result = false;

        List<Wiring> wires = this.getWires();
        wires.removeIf(Predicate.not(Wiring::isValid));

        for (int i = 0; i < wires.size(); i++) {
            Wiring wire = wires.get(i);
            if (source.getWireMedium() != null && source.getWireMedium().getReference().equals(wire.getReference())) continue;

            Source oldSource = source;
            if (i < wires.size()-1) source = source.copy();

            wire.relay(oldSource, this);

            result = true;
        }

        return result;
    }

    default boolean hasWire(Wiring wire) {
        return this.hasWire(wire.getFrom().orElse(null), wire.getTo().orElse(null));
    }

    default boolean hasWire(UUID from, UUID to) {
        if (from == null || to == null) return false;

        for (Wiring otherWire : this.getWires()) {
            Optional<UUID> otherFrom = otherWire.getFrom();
            Optional<UUID> otherTo = otherWire.getTo();
            if (otherFrom.isEmpty() || otherTo.isEmpty()) continue;

            if (otherFrom.get().equals(from) && otherTo.get().equals(to)) return true;
            if (otherFrom.get().equals(to) && otherTo.get().equals(from)) return true;
        }

        return false;
    }

    default void connect(Wiring wire) {
        this.getWires().add(wire);
    }
    default void disconnect(Wiring wire) {
        this.getWires().removeIf(otherWire -> otherWire.equals(wire));
    }
    default void disconnect(UUID wire) {
        this.getWires().removeIf(otherWire -> otherWire.getReference().equals(wire));
    }

    default UUID getReference() {
        return this.getRouter().getReference();
    }
    default short getIdentifier() {
        return this.getRouter().getIdentifier();
    }

    default void shortCircuit() {
        Router router = this.getRouter();
        WorldlyPosition location = router.getLocation();

        if (!location.isClientSide()) ServerSimpleRadioApi.getInstance().shortAt(location);

        for (Wiring wire : getWires().stream().toList()) {
            wire.burnOut();
        }

        this.getWires().clear();
        this.getRouter().getRouters();
    }

    /**
     * Override this to <i>expose</i> a given router to wires. <br>
     * Allows blocks with more than one router to choose which of their routers wires will connect to.
     * @return The router to be exposed
     */
    Router getRouter();

    default List<Wiring> getWires() {
        Router router = getRouter();
        if (router == null) return List.of();
        return router.getWires();
    }
}
