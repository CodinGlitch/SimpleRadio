package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.Listeners;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.routers.Listener;
import com.codinglitch.simpleradio.routers.RouterContainer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

public class ListenersImpl implements Listeners {
    private static final RouterContainer<Listener> LISTENERS = new RouterContainer<>();

    public static void garbageCollect() {
        RadioManager.validate(LISTENERS);
    }
    public static void close() {
        LISTENERS.clear();
    }

    @Override
    public List<Listener> get() {
        return new ArrayList<>(LISTENERS);
    }

    @Override
    public Map<Float, Listener> getAt(WorldlyPosition at) {
        TreeMap<Float, Listener> qualified = new TreeMap<>();
        for (Listener listener : get()) {
            Vector3f position;
            if (listener.getPosition() != null) {
                position = listener.getPosition().position();
            } else if (listener.getOwner() != null) {
                position = listener.getOwner().position().toVector3f();
            } else continue;

            float distance = position.distance(at);
            if (distance > listener.getRange()) continue;

            qualified.put(distance, listener);
        }
        return qualified;
    }

    @Override
    public void remove(Listener Listener) {
        remove(Listener::equals);
    }
    @Override
    public void remove(Predicate<Listener> criteria) {
        LISTENERS.removeIf(criteria);
    }
    @Override
    public void remove(short identifier) {
        LISTENERS.remove(identifier);
    }

    @Override
    public void remove(Entity owner) {
        remove(listener -> owner.equals(listener.getOwner()));
    }
    @Override
    public void remove(WorldlyPosition location) {
        remove(listener -> location.equals(listener.getPosition()));
    }
    @Override
    public void remove(UUID id) {
        remove(listener -> id.equals(listener.getReference()));
    }

    @Override
    public RadioListener get(Entity owner) {
        return get(listener -> owner.equals(listener.getOwner()));
    }
    @Override
    public RadioListener get(WorldlyPosition location) {
        return get(listener -> location.equals(listener.getPosition()));
    }
    @Override
    public RadioListener get(UUID id) {
        return get(listener -> id.equals(listener.getReference()));
    }
    @Override
    public RadioListener get(Predicate<Listener> filter) {
        Optional<Listener> result = LISTENERS.stream().filter(filter).findFirst();
        return (RadioListener) result.orElse(null);
    }

    @Override
    public RadioListener getOrCreate(Entity owner, @Nullable UUID id) {
        boolean isClient = owner.level().isClientSide;

        RadioListener listener = null;//isClient ? ClientRadioManager.getListener(owner) : getListener(owner);
        if (listener == null) listener = isClient ? ClientRadioManager.getListener(id) : get(id);

        return listener != null ? listener : new RadioListener(owner, id);
    }

    @Override
    public RadioListener getOrCreate(Entity owner) { return getOrCreate(owner, null); }
    @Override
    public RadioListener getOrCreate(WorldlyPosition location, @Nullable UUID id) {
        boolean isClient = location.level.isClientSide;

        RadioListener listener = null;//isClient ? ClientRadioManager.getListener(location) : getListener(location);
        if (listener == null) listener = isClient ? ClientRadioManager.getListener(id) : get(id);

        return listener != null ? listener : new RadioListener(location, id);
    }
    @Override
    public RadioListener getOrCreate(WorldlyPosition location) { return getOrCreate(location, null); }

    @Override
    public RadioListener register(Listener listener) {
        if (listener.getPosition() != null) {
            if (listener.getPosition().isClientSide()) {
                CommonSimpleRadio.warn("Attempted to register a client-sided Listener on the server; cancelling");
                return null;
            }
        }

        RadioManager.getInstance().putRouter(LISTENERS, listener);
        return (RadioListener) listener;
    }
}
