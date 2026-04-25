package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.*;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class ClientSimpleRadioApi extends SimpleRadioApi {
    private static ClientSimpleRadioApi INSTANCE;
    public ClientSimpleRadioApi() {
        INSTANCE = this;
    }

    public static ClientSimpleRadioApi getInstance() {
        return INSTANCE;
    }

    public abstract List<Router> getRouters();

    public abstract Router getRouter(Predicate<Router> criteria);

    public abstract Router getRouter(short identifier);

    public abstract Router getRouter(UUID reference, @Nullable String type);
    public abstract Router getRouter(UUID reference);

    public abstract Router getRouter(Entity owner);

    public abstract Router getRouter(WorldlyPosition location);

    public abstract Listener getListener(UUID uuid);
    public abstract Listener getListener(Entity owner);
    public abstract Listener getListener(WorldlyPosition location);

    public abstract Speaker getSpeaker(UUID uuid);
    public abstract Speaker getSpeaker(Entity owner);
    public abstract Speaker getSpeaker(WorldlyPosition location);

    public abstract Receiver getReceiver(UUID uuid);
    public abstract Receiver getReceiver(Entity owner);
    public abstract Receiver getReceiver(WorldlyPosition location);

    public abstract Transmitter getTransmitter(UUID uuid);
    public abstract Transmitter getTransmitter(Entity owner);
    public abstract Transmitter getTransmitter(WorldlyPosition location);

    public abstract Router removeRouter(Predicate<Router> predicate);

    public abstract Router removeRouter(Router router);

    public abstract Router removeRouter(UUID reference);
    public abstract Router removeRouter(UUID reference, @Nullable String type);

    public abstract Router removeRouter(Entity owner);
    public abstract Router removeRouter(Entity owner, @Nullable String type);

    public abstract Router removeRouter(WorldlyPosition location);
    public abstract Router removeRouter(WorldlyPosition location, @Nullable String type);

    public abstract Listener removeListener(UUID uuid);
    public abstract Speaker removeSpeaker(UUID uuid);
    public abstract Receiver removeReceiver(UUID uuid);
    public abstract Transmitter removeTransmitter(UUID uuid);
}
