package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.*;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Routes RadioSources to other routers.
 */
public class RadioRouter implements Socket {
    public enum Type { //TODO: turn this into a registry of sorts
        LISTENER(RadioListener.class),
        SPEAKER(RadioSpeaker.class),
        TRANSMITTER(RadioTransmitter.class),
        RECEIVER(RadioReceiver.class);

        final Class<? extends RadioRouter> matchingClass;

        Type(Class<? extends RadioRouter> matchingClass) {
            this.matchingClass = matchingClass;
        }

        public static Type byName(String name) {
            return Arrays.stream(Type.values())
                    .filter(value -> value.name().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
        }

        public static Type byClass(Class<?> clazz) {
            return Arrays.stream(Type.values())
                    .filter(value -> value.matchingClass.equals(clazz))
                    .findFirst().orElse(null);
        }
        public static <T extends RadioRouter> Type byInstance(T instance) {
            return byClass(instance.getClass());
        }
    }

    public ArrayList<Wire> wires = new ArrayList<>();

    public List<RadioRouter> routers = new ArrayList<>();
    public Predicate<RadioRouter> routerCriteria;
    public Function<RadioSource, Boolean> routerAcceptor; // kept just in case

    public boolean active = true;
    public boolean distributes = false;
    public boolean valid = true;
    public UUID id;
    public Entity owner;
    public WorldlyPosition location;
    public Vector3f oldPosition = new Vector3f();
    public Vector3f velocity = new Vector3f();

    public Class<?> link;

    public Quaternionf rotation = null;
    public Vec3 connectionOffset = Vec3.ZERO; // A given offset for the rendering of wires connected to it. Relative to rotation if given.

    public RadioRouter(UUID id) {
        this.id = id;
    }
    public RadioRouter() {
        this(UUID.randomUUID());
    }

    public RadioRouter(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioRouter(WorldlyPosition location, UUID uuid) {
        this(uuid);
        this.location = location;
    }

    @Nullable
    public static RadioReceiver getRouterFromReceivers(UUID uuid) {
        for (Frequency frequency : Frequency.getFrequencies()) {
            for (RadioReceiver receiver : frequency.receivers) {
                if (receiver.id.equals(uuid)) return receiver;
            }
        }
        return null;
    }

    @Nullable
    public static RadioTransmitter getRouterFromTransmitters(UUID uuid) {
        for (Frequency frequency : Frequency.getFrequencies()) {
            for (RadioTransmitter transmitter : frequency.transmitters) {
                if (transmitter.id.equals(uuid)) return transmitter;
            }
        }
        return null;
    }

    @Nullable
    public static RadioRouter getRouterFromUUID(UUID uuid, @Nullable Type type) {
        if (type == null) return RadioManager.getRouter(uuid);

        return switch (type) {
            case SPEAKER -> RadioManager.getSpeaker(uuid);
            case LISTENER -> RadioManager.getListener(uuid);
            case TRANSMITTER -> getRouterFromTransmitters(uuid);
            case RECEIVER -> getRouterFromReceivers(uuid);
        };
    }

    @Override
    public RadioRouter getRouter() {
        return this;
    }

    @Override
    public UUID getID() {
        return this.id;
    }

    @Override
    public ArrayList<Wire> getWires() {
        return this.wires;
    }

    public void allowDistribution() {
        this.distributes = true;
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

    public RadioRouter tryAddRouter(RadioRouter router) {
        RadioRouter existingRouter = getRouter(router.id);
        if (existingRouter != null) return existingRouter;

        routers.add(router);
        return router;
    }
    public RadioRouter getRouter(UUID id) {
        return routers.stream().filter(router -> router.id.equals(id)).findFirst().orElse(null);
    }

    public void updateLocation(WorldlyPosition location) {
    }

    public void tick(int tickCount) {
        if (location != null) {
            Services.COMPAT.modifyPosition(location);
            this.updateLocation(location);

            Vector3f currentPosition = location.position();
            if (currentPosition != oldPosition) {
                currentPosition.sub(oldPosition, velocity);
            } else {
                velocity.set(0);
            }
            oldPosition.set(currentPosition);
        }
    }

    public void accept(RadioSource source) {
        this.route(source);
    }

    public RadioSource prepareSource(RadioSource source, RadioRouter destination) {
        WorldlyPosition from = this.getLocation();
        WorldlyPosition to = destination.getLocation();
        if (from.equals(to)) return source;

        source.travel(from, to, getFrequency());
        return source;
    }

    public boolean shouldRouteTo(RadioSource source, RadioRouter destination) {
        return true;
    }

    public void route(RadioSource source, Predicate<RadioRouter> criteria) {
        if (!this.active) return;

        if (!source.isValid()) {
            CommonSimpleRadio.warn("Invalid source; discarded");
            return;
        }

        if (routerAcceptor != null) {
            if (routerAcceptor.apply(source))
                source = source.copy();
        }

        if (distributes) {
            if (this.distribute(source)) source = source.copy();
        }

        for (int i = 0; i < routers.size(); i++) {
            RadioRouter router = routers.get(i);
            if (!shouldRouteTo(source, router)) continue;

            source = this.prepareSource(source, router);
            if (criteria != null) {
                if (!criteria.test(router)) continue;
            }

            RadioSource oldSource = source;
            if (i < routers.size()-1) source = source.copy();
            router.accept(oldSource);
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
                flag = Auricular.validate(location, Speaking.class);
            } else if (this instanceof RadioListener) {
                flag = Auricular.validate(location, Listening.class);
            } else if (this instanceof RadioReceiver) {
                flag = Frequencing.validate(location, Receiving.class, null);
            } else if (this instanceof RadioTransmitter) {
                flag = Frequencing.validate(location, Transmitting.class, null);
            } else {
                flag = this.link != null && RadioManager.verifyLocationCollection(location, this.link);
            }

            if (!flag) {
                invalidate();
                return false;
            }
        } else {
            boolean flag = true;
            if (this instanceof RadioSpeaker) {
                flag = Auricular.validate(owner, Speaking.class);
            } else if (this instanceof RadioListener) {
                flag = Auricular.validate(owner, Listening.class);
            } else if (this instanceof RadioReceiver) {
                flag = Frequencing.validate(owner, Receiving.class, null);
            } else if (this instanceof RadioTransmitter) {
                flag = Frequencing.validate(owner, Transmitting.class, null);
            } else {
                flag = this.link != null && RadioManager.verifyEntityCollection(owner, stack -> stack.getItem().getClass().isInstance(this.link));
            }

            if (!flag) {
                invalidate();
                return false;
            }
        }
        return true;
    }
}
