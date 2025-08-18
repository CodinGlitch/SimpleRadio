package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.Source;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    Router addRouter(Router router);

    @Nullable
    WorldlyPosition getPosition();

    @Nullable
    Entity getOwner();
    Router getRouter(UUID id);
    Vec3 getConnectionPosition();

    boolean isActive();
    Vec3 getConnectionOffset();
    Class<?> getLink();
    Vector3f getVelocity();
    float getActivity();
    int getRedstoneMappedActivity();
    Quaternionf getRotation();

    void allowDistribution();
    void setOwner(Entity owner);
    void setActive(boolean active);
    void setLink(Class<?> link);
    void setConnectionOffset(Vec3 connectionOffset);
    void setPosition(WorldlyPosition position);
    void setRoutingCriteria(BiPredicate<Source, Router> criteria);
    void setAcceptingCriteria(Predicate<Source> criteria);

    double distanceTo(Router other);

    void route(Source source);
    void accept(Source source);

    boolean validate();
    void invalidate();
}
