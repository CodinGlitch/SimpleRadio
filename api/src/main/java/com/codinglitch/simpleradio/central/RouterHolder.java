package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.routers.Router;
import com.codinglitch.simpleradio.routers.RouterContainer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public interface RouterHolder<R extends Router> {
    List<R> get();
    RouterContainer<R> contents();

    default R remove(Predicate<R> criteria) {
        RouterContainer<R> list = contents();
        Optional<R> first = list.stream()
                .filter(criteria)
                .findFirst();

        list.removeIf(criteria);
        return first.orElse(null);
    }
    default R remove(R R) {
        get().remove(R);
        return R;
    }
    default R remove(short identifier) {
        return get().remove(identifier);
    }

    default R remove(Entity owner) {
        return remove(entry -> owner.equals(entry.getOwner()));
    }
    default R remove(WorldlyPosition location) {
        return remove(entry -> location.equals(entry.getPosition()));
    }
    default R remove(UUID id) {
        return remove(entry -> id.equals(entry.getReference()));
    }

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
