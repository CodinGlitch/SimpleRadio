package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.Source;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface Router {
    UUID getReference();

    short getIdentifier();

    @Nullable
    Frequency getFrequency();

    WorldlyPosition getLocation();

    Router tryAddRouter(Router router);
    Router getRouter(UUID id);

    Vec3 getConnectionPosition();

    void setRoutingCriteria(BiPredicate<Source, Router> criteria);
    void setAcceptingCriteria(Predicate<Source> criteria);

    double distanceTo(Router other);

    void route(Source source);
    void accept(Source source);
}
