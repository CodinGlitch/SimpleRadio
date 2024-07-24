package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.core.central.Transmitting;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class RadioListener extends RadioRouter {
    private static final List<RadioListener> listeners = new ArrayList<>();

    public static List<RadioListener> getListeners() {
        return listeners;
    }

    public static void removeListener(RadioListener listener) {
        listeners.remove(listener);
    }
    public static void removeListener(Entity owner) {
        listeners.removeIf(listener -> listener.owner == owner);
    }
    public static void removeListener(WorldlyPosition location) {
        listeners.removeIf(listener -> listener.location != null && listener.location.equals(location));
    }
    public static void removeListener(UUID id) {
        listeners.removeIf(listener -> listener.id == id);
    }

    public static RadioListener getListener(Entity owner) {
        return listeners.stream().filter(listener -> listener.owner == owner)
                .findFirst().orElse(null);
    }
    public static RadioListener getListener(WorldlyPosition location) {
        return listeners.stream().filter(listener -> listener.location == location)
                .findFirst().orElse(null);
    }
    public static RadioListener getListener(UUID id) {
        return listeners.stream().filter(listener -> listener.id == id)
                .findFirst().orElse(null);
    }

    public static RadioListener getOrCreateListener(Entity owner, @Nullable UUID id) {
        RadioListener listener = getListener(owner);
        if (listener == null) listener = getListener(id);

        return listener != null ? listener : new RadioListener(owner);
    }
    public static RadioListener getOrCreateListener(Entity owner) { return getOrCreateListener(owner, null); }

    public static RadioListener getOrCreateListener(WorldlyPosition location, @Nullable UUID id) {
        RadioListener listener = getListener(location);
        if (listener == null) listener = getListener(id);

        return listener != null ? listener : new RadioListener(location);
    }
    public static RadioListener getOrCreateListener(WorldlyPosition location) { return getOrCreateListener(location, null); }

    public static void garbageCollect() {
        listeners.removeIf(Predicate.not(RadioListener::validate));
        listeners.removeIf(listener -> listener.owner == null && listener.location == null);
    }

    private UnaryOperator<RadioSource> dataTransformer;

    public float range = 8;

    protected RadioListener(UUID id) {
        super(id);
        listeners.add(this);
    }
    protected RadioListener() {
        this(UUID.randomUUID());
    }

    public RadioListener(Entity owner) {
        this(owner, UUID.randomUUID());
    }
    public RadioListener(Entity owner, UUID uuid) {
        this(uuid);
        this.owner = owner;
    }
    public RadioListener(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioListener(WorldlyPosition location, UUID uuid) {
        this(uuid);
        this.location = location;
    }

    public void transformer(UnaryOperator<RadioSource> transformer) {
        this.dataTransformer = transformer;
    }

    public void onData(RadioSource source) {
        if (dataTransformer != null) {
            source = dataTransformer.apply(source);
        }

        this.route(source);
    }
}
