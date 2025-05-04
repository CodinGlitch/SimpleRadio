package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface RouterHolder<R extends Router> {
    List<R> get();

    void remove(R R);
    void remove(Predicate<R> criteria);
    void remove(short identifier);

    void remove(Entity owner);
    void remove(WorldlyPosition location);
    void remove(UUID id);

    R get(Entity owner);
    R get(WorldlyPosition location);
    R get(UUID id);
    R get(Predicate<R> filter);

    R getOrCreate(Entity owner, @Nullable UUID id);

    R getOrCreate(Entity owner);
    R getOrCreate(WorldlyPosition location, @Nullable UUID id);
    R getOrCreate(WorldlyPosition location);

    R register(R R);
}
