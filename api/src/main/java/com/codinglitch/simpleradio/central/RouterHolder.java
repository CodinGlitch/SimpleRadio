package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface RouterHolder<R extends Router> {
    List<R> get();

    default R remove(Predicate<R> criteria) {
        List<R> list = get();
        List<R> removal = list.stream()
                .filter(criteria)
                .toList();

        if (removal.isEmpty()) return null;

        removal.forEach(list::remove);
        return removal.stream().findFirst().get();
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
