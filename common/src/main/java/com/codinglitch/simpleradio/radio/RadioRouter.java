package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.core.central.*;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class RadioRouter {
    public List<RadioRouter> routers = new ArrayList<>();
    public Predicate<RadioRouter> routerCriteria;

    public boolean valid = true;
    public UUID id;
    public Entity owner;
    public WorldlyPosition location;

    public RadioRouter(UUID id) {
        this.id = id;
    }
    public RadioRouter() {
        this(UUID.randomUUID());
    }

    @Nullable
    public Frequency getFrequency() {
        return null;
    }

    public WorldlyPosition getLocation() {
        if (this.location != null) {
            return this.location;
        } else if (this.owner != null) {
            return new WorldlyPosition((float) owner.getX(), (float) owner.getY(), (float) owner.getZ(), owner.level());
        }
        return null;
    }

    public void updateLocation(WorldlyPosition location) {
    }

    public void serverTick(int tickCount) {
        if (location != null) {
            Services.COMPAT.modifyPosition(location);
            this.updateLocation(location);
        }
    }

    public void accept(RadioSource source) {

    }

    public RadioSource prepareSource(RadioSource source, RadioRouter router) {
        WorldlyPosition from = this.getLocation();
        WorldlyPosition to = router.getLocation();
        if (from.equals(to)) return source;

        source.travel(from, to, getFrequency());
        return source;
    }

    public void route(RadioSource source, Predicate<RadioRouter> criteria) {
        for (int i = 0; i < routers.size(); i++) {
            RadioRouter router = routers.get(i);

            source = this.prepareSource(source, router);
            if (criteria != null) {
                if (!criteria.test(router)) continue;
            }

            router.accept(source);

            if (i < routers.size()-1) {
                source = source.copy();
            }
        }
    }

    public void route(RadioSource source) {
        this.route(source, this.routerCriteria);
    }

    public void invalidate() {
        this.valid = false;
    }

    public boolean validate() {
        if (!valid) return false;

        if (owner == null) {
            if (location == null) {
                invalidate();
                return false;
            }

            boolean flag = true;
            if (this instanceof RadioSpeaker) {
                flag = Auditory.validate(location, Speaking.class);
            } else if (this instanceof RadioListener) {
                flag = Auditory.validate(location, Listening.class);
            } else if (this instanceof RadioReceiver) {
                flag = Frequencing.validate(location, Receiving.class, null);
            } else if (this instanceof RadioTransmitter) {
                flag = Frequencing.validate(location, Transmitting.class, null);
            }

            if (!flag) {
                invalidate();
                return false;
            }
        } else {
            boolean flag = true;
            if (this instanceof RadioSpeaker) {
                flag = Auditory.validate(owner, Speaking.class);
            } else if (this instanceof RadioListener) {
                flag = Auditory.validate(owner, Listening.class);
            } else if (this instanceof RadioReceiver) {
                flag = Frequencing.validate(owner, Receiving.class, null);
            } else if (this instanceof RadioTransmitter) {
                flag = Frequencing.validate(owner, Transmitting.class, null);
            }

            if (!flag) {
                invalidate();
                return false;
            }
        }
        return true;
    }
}
