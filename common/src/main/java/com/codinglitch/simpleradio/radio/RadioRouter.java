package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.api.central.*;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundActivityPacket;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Routes RadioSources to other routers.
 */
public class RadioRouter implements Socket {
    public static class Compiled<E> extends LinkedList<E> {
        @Override
        public boolean add(E value) {
            super.add(value);
            while (size() > 20) super.remove();
            return true;
        }
    }

    public ArrayList<Wire> wires = new ArrayList<>();

    public List<RadioRouter> routers = new ArrayList<>();
    public Function<RadioSource, Boolean> routerAcceptor; // kept just in case

    public BiPredicate<RadioSource, RadioRouter> routeCriteria;
    public Predicate<RadioSource> acceptCriteria;

    public boolean active = true;
    public boolean distributes = false;
    public boolean valid = true;

    public short identifier;
    public UUID reference;
    public Entity owner;
    public WorldlyPosition location;
    public Vector3f oldPosition = new Vector3f();
    public Vector3f velocity = new Vector3f();

    public float activity = 0;
    public int activityTime = 0;

    public float compiledActivity = 0;
    public int compiledSamples = 0;

    public Class<?> link;

    public Quaternionf rotation = null;
    public Vec3 connectionOffset = Vec3.ZERO; // A given offset for the rendering of wires connected to it. Relative to rotation if given.

    public RadioRouter(UUID reference) {
        this.reference = reference;
    }
    public RadioRouter() {
        this(UUID.randomUUID());
    }

    public RadioRouter(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioRouter(WorldlyPosition location, UUID reference) {
        this(reference);
        this.location = location;
    }

    @Nullable
    public static RadioReceiver getRouterFromReceivers(UUID reference) {
        for (Frequency frequency : Frequency.getFrequencies()) {
            RadioReceiver receiver = frequency.getReceiver(reference);
            if (receiver != null) return receiver;
        }
        return null;
    }

    @Nullable
    public static RadioTransmitter getRouterFromTransmitters(UUID reference) {
        for (Frequency frequency : Frequency.getFrequencies()) {
            RadioTransmitter transmitter = frequency.getTransmitter(reference);
            if (transmitter != null) return transmitter;
        }
        return null;
    }

    @Override
    public RadioRouter getRouter() {
        return this;
    }

    @Override
    public UUID getReference() {
        return this.reference;
    }

    public short getIdentifier() {
        return this.identifier;
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

    public double distanceTo(RadioRouter other) {
        return this.getLocation().distance(other.getLocation());
    }

    public Vec3 getConnectionPosition() {
        Vector3f translatedOffset = rotation == null ? connectionOffset.toVector3f() : rotation.transform(connectionOffset.toVector3f());

        return new Vec3(getLocation().position()).add(translatedOffset.x, translatedOffset.y, translatedOffset.z);
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
        RadioRouter existingRouter = getRouter(router.reference);
        if (existingRouter != null) return existingRouter;

        routers.add(router);
        return router;
    }
    public RadioRouter getRouter(UUID id) {
        return routers.stream().filter(router -> router.reference.equals(id)).findFirst().orElse(null);
    }

    //this method is so dumb bro
    public void updateLocation(WorldlyPosition location) {
    }

    public void updateRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    public void tick(int tickCount) {
        if (location != null) {
            this.updateRotation(Services.COMPAT.modifyRotation(location, rotation));
            this.updateLocation(Services.COMPAT.modifyPosition(location));

            Vector3f currentPosition = location.position();
            if (currentPosition != oldPosition) {
                currentPosition.sub(oldPosition, velocity);
            } else {
                velocity.set(0);
            }
            oldPosition.set(currentPosition);
        } else if (owner != null) {
            this.updateLocation(WorldlyPosition.of(owner.position().toVector3f(), owner.level()));
        }

        if (!this.active) {
            this.activity = 0;
            this.activityTime = 0;

            this.compiledSamples = 0;
            this.compiledActivity = 0;
        } else {
            if (this.activityTime > 0) this.activityTime--;
        }
    }

    public void accept(RadioSource source) {
        if (!this.active) return;
        if (acceptCriteria != null && !acceptCriteria.test(source)) return;
        this.route(source);
    }

    public RadioSource prepareSource(RadioSource source, RadioRouter destination) {
        if (this.getLocation().equals(destination.getLocation())) return source;

        source.travel(this, destination, getFrequency());
        return source;
    }

    public boolean shouldRouteTo(RadioSource source, RadioRouter destination) {
        return true;
    }

    public void route(RadioSource source, @Nullable Predicate<RadioRouter> criteria) {
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

            if (criteria != null) {
                if (!criteria.test(router)) continue;
            }
            if (!shouldRouteTo(source, router)) continue;

            if (routeCriteria != null && !routeCriteria.test(source, router)) continue;

            if (source.willShort(router)) {
                router.shortCircuit();
                continue;
            }

            source = this.prepareSource(source, router);

            RadioSource oldSource = source;
            if (i < routers.size()-1) source = source.copy();
            router.accept(oldSource);
        }
    }

    public void route(RadioSource source) {
        this.route(source, null);
    }

    public int getRedstoneMappedActivity() {
        return (int) Math.clamp(0, 15, Math.round(this.activity / SimpleRadioLibrary.SERVER_CONFIG.router.activityRedstoneFactor));
    }

    public void compileActivity(RadioSource source) {
        if (!this.active) return;

        if (source.data == null) {
            this.activity = source.activity;
            compiledActivity = 0;
            compiledSamples = 0;
        } else {
            compiledActivity += source.activity;
            if (compiledSamples++ >= SimpleRadioLibrary.SERVER_CONFIG.router.compileAmount) {
                this.activity = Math.sqrt(compiledActivity);
                compiledActivity = 0;
                compiledSamples = 0;
            }
        }

        if (activityTime < SimpleRadioLibrary.SERVER_CONFIG.router.activityForgiveness) {
            this.activityTime = SimpleRadioLibrary.SERVER_CONFIG.router.activityTime;

            WorldlyPosition location = getLocation();
            if (!location.isClientSide()) {
                for (Player player : location.level.players()) {
                    if (location.position().distance((float) player.getX(), (float) player.getY(), (float) player.getZ()) <= 100) {
                        Services.NETWORKING.sendToPlayer((ServerPlayer) player, new ClientboundActivityPacket(activity, this.getIdentifier()));
                    }
                }
            }
        }
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
                flag = Auricular.validate(location, this.link != null ? this.link : Speaking.class);
            } else if (this instanceof RadioListener) {
                flag = Auricular.validate(location, this.link != null ? this.link : Listening.class);
            } else if (this instanceof RadioReceiver) {
                flag = Frequencing.validate(location, this.link != null ? this.link : Receiving.class, null);
            } else if (this instanceof RadioTransmitter) {
                flag = Frequencing.validate(location, this.link != null ? this.link : Transmitting.class, null);
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
                flag = Auricular.validate(owner, this.link != null ? this.link : Speaking.class);
            } else if (this instanceof RadioListener) {
                flag = Auricular.validate(owner, this.link != null ? this.link : Listening.class);
            } else if (this instanceof RadioReceiver) {
                flag = Frequencing.validate(owner, this.link != null ? this.link : Receiving.class, null);
            } else if (this instanceof RadioTransmitter) {
                flag = Frequencing.validate(owner, this.link != null ? this.link : Transmitting.class, null);
            } else {
                flag = this.link != null && RadioManager.verifyEntityCollection(owner, stack -> this.link.isAssignableFrom(stack.getItem().getClass()));
            }

            if (!flag) {
                invalidate();
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.getIdentifier() + "]" + this.getLocation().toString();
    }
}
