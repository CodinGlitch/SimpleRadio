package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Router {
    UUID getReference();

    short getIdentifier();

    @Nullable
    Frequency getFrequency();

    WorldlyPosition getLocation();

    Router tryAddRouter(Router router);
    Router getRouter(UUID id);

    Vec3 getConnectionPosition();

    double distanceTo(Router other);
}
