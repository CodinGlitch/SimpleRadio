package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.radio.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.ArrayList;
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
     * Distribute a {@link RadioSource} along every wire connected to this socket.
     * @param source The {@link RadioSource} to distribute
     * @return Whether or not the source was distributed across any wires.
     */
    default boolean distribute(RadioSource source) {
        boolean result = false;

        List<Wire> wires = this.getWires();
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
        return this.hasWire(wire.getFrom().orElse(null), wire.getTo().orElse(null));
    }

    default boolean hasWire(UUID from, UUID to) {
        if (from == null || to == null) return false;

        for (Wire otherWire : this.getWires()) {
            Optional<UUID> otherFrom = otherWire.getFrom();
            Optional<UUID> otherTo = otherWire.getTo();
            if (otherFrom.isEmpty() || otherTo.isEmpty()) continue;

            if (otherFrom.get().equals(from) && otherTo.get().equals(to)) return true;
            if (otherFrom.get().equals(to) && otherTo.get().equals(from)) return true;
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

    default UUID getReference() {
        return this.getRouter().getReference();
    }
    default short getIdentifier() {
        return this.getRouter().getIdentifier();
    }

    default List<Wire> getWires() {
        RadioRouter router = this.getRouter();
        if (router == null) return List.of();
        return router.getWires();
    }

    default void shortCircuit() {
        RadioRouter router = this.getRouter();
        WorldlyPosition location = router.getLocation();

        Level level = location.level;
        if (level instanceof ServerLevel serverLevel) shortAt(serverLevel, location);

        for (Object wire : router.getWires().toArray()) {
            ((Wire) wire).burnOut();
        }
        this.getWires().clear();
    }

    static void shortAt(ServerLevel level, Vector3f location) {
        level.playSound(null, location.x, location.y, location.z, SimpleRadioSounds.SHORT_CIRCUIT, SoundSource.BLOCKS, 0.3f, 0.9f + level.random.nextFloat()*0.2f);

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                location.x, location.y, location.z, 10,
                -0.2+level.random.nextDouble()*0.4, -0.2+level.random.nextDouble()*0.4, -0.2+level.random.nextDouble()*0.4, 1
        );
        level.sendParticles(ParticleTypes.CRIT,
                location.x, location.y, location.z, 8,
                -0.2+level.random.nextDouble()*0.4, -0.2+level.random.nextDouble()*0.4, -0.2+level.random.nextDouble()*0.4, 1
        );
        level.sendParticles(ParticleTypes.POOF,
                location.x, location.y, location.z, 5,
                0.2d, 0.2d, 0.2d, 0.1d
        );
    }

    /**
     * Override this to <i>expose</i> a given router to wires. <br>
     * Allows blocks with more than one router to choose which of their routers wires will connect to.
     * @return The router to be exposed
     */
    RadioRouter getRouter();
}
