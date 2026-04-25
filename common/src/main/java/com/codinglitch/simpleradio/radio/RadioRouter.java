package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.*;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundActivityPacket;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.routers.Receiver;
import com.codinglitch.simpleradio.routers.Router;
import com.codinglitch.simpleradio.routers.Transmitter;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.codinglitch.simpleradio.core.SimpleRadioComponents.REFERENCE;

/**
 * Routes RadioSources to other routers.
 */
public class RadioRouter implements Socket, Router {
    public static class Compiled<E> extends LinkedList<E> {
        @Override
        public boolean add(E value) {
            super.add(value);
            while (size() > 20) super.remove();
            return true;
        }
    }

    private Map<UUID, OpusDecoder> decoders;
    private Map<UUID, OpusEncoder> encoders;

    public List<Wiring> wires = new ArrayList<>();
    public List<Router> routers = new ArrayList<>();
    public Function<RadioSource, Boolean> routerAcceptor; // kept just in case

    public BiPredicate<Source, Router> routeCriteria;
    public Predicate<Source> acceptCriteria;

    public boolean active = true;
    public boolean distributes = false;
    public boolean valid = true;

    public short identifier;
    public UUID reference;

    public Entity owner;
    public WorldlyPosition position;
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
        this.position = location;
    }

    @Nullable
    public static Receiver getRouterFromReceivers(UUID reference) {
        for (Frequency frequency : RadioManager.getInstance().frequencies().get()) {
            Receiver receiver = frequency.getReceiver(reference);
            if (receiver != null) return receiver;
        }
        return null;
    }

    @Nullable
    public static Transmitter getRouterFromTransmitters(UUID reference) {
        for (Frequency frequency : RadioManager.getInstance().frequencies().get()) {
            Transmitter transmitter = frequency.getTransmitter(reference);
            if (transmitter != null) return transmitter;
        }
        return null;
    }

    @Override
    public void setRoutingCriteria(BiPredicate<Source, Router> criteria) {
        this.routeCriteria = criteria;
    }
    @Override
    public void setAcceptingCriteria(Predicate<Source> criteria) {
        this.acceptCriteria = criteria;
    }

    @Override
    public RadioRouter getRouter() {
        return this;
    }

    @Override
    public UUID getReference() {
        return this.reference;
    }

    @Override
    public short getIdentifier() {
        return this.identifier;
    }

    @Override
    public boolean isActive() {
        return active;
    }
    @Override
    public boolean isValid() {
        return valid;
    }
    @Override
    public Vec3 getConnectionOffset() {
        return connectionOffset;
    }
    @Override
    public Class<?> getLink() {
        return link;
    }

    @Override
    public List<Wiring> getWires() {
        return this.wires;
    }

    public OpusDecoder getDecoder(UUID sender) {
        if (decoders == null) decoders = new ConcurrentHashMap<>();
        return decoders.computeIfAbsent(sender, uuid -> CommonRadioPlugin.serverApi.createDecoder());
    }

    public OpusEncoder getEncoder(UUID sender) {
        if (encoders == null) encoders = new ConcurrentHashMap<>();
        return encoders.computeIfAbsent(sender, uuid -> CommonRadioPlugin.serverApi.createEncoder());
    }

    @Nullable
    @Override
    public Frequency getFrequency() {
        return null;
    }

    @Nullable
    @Override
    public Boolean isClientSide() {
        if (owner != null) return owner.level().isClientSide();
        if (position != null) return position.isClientSide();
        return null;
    }

    @Override
    public WorldlyPosition getLocation() {
        if (this.position != null) {
            return this.position;
        } else if (this.owner != null) {
            return new WorldlyPosition((float) owner.getX(), (float) owner.getY(), (float) owner.getZ(), owner.level());
        }
        return null;
    }

    @Nullable
    @Override
    public WorldlyPosition getPosition() {
        return this.position;
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return this.owner;
    }

    @Override
    public Router getRouter(UUID id) {
        return routers.stream().filter(router -> router.getReference().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Router> getRouters() {
        return routers;
    }

    @Override
    public Vec3 getConnectionPosition() {
        Vector3f translatedOffset = rotation == null ? connectionOffset.toVector3f() : rotation.transform(connectionOffset.toVector3f());

        return new Vec3(getLocation().position()).add(translatedOffset.x, translatedOffset.y, translatedOffset.z);
    }

    @Override
    public Vector3f getVelocity() {
        return velocity;
    }

    @Override
    public float getActivity() {
        return activity;
    }

    @Override
    public int getActivityTime() {
        return activityTime;
    }

    @Override
    public int getRedstoneMappedActivity() {
        return (int) Math.clamp(0, 15, Math.round(this.activity / SimpleRadioLibrary.SERVER_CONFIG.router.activityRedstoneFactor));
    }

    @Override
    public Quaternionf getRotation() {
        return rotation;
    }

    @Override
    public void allowDistribution() {
        this.distributes = true;
    }

    @Override
    public void setOwner(Entity owner) {
        this.owner = owner;
    }
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    @Override
    public void setLink(Class<?> link) {
        this.link = link;
    }
    @Override
    public void setConnectionOffset(Vec3 connectionOffset) {
        this.connectionOffset = connectionOffset;
    }
    @Override
    public void setPosition(WorldlyPosition position) {
        this.position = position;
    }
    @Override
    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }


    @Override
    public double distanceTo(Router other) {
        return distanceTo((RadioRouter) other);
    }
    public double distanceTo(RadioRouter other) {
        return this.getLocation().distance(other.getLocation());
    }

    @Override
    public Router tryAddRouter(Router router) {
        return tryAddRouter((RadioRouter) router);
    }
    public Router tryAddRouter(RadioRouter router) {
        Router existingRouter = getRouter(router.reference);
        if (existingRouter != null) return existingRouter;

        return addRouter(router);
    }

    @Override
    public Router addRouter(Router router) {
        routers.add(router);
        return router;
    }

    @Override
    public void accept(Source source) {
        CompatCore.acceptSource(this, source);
        this.take(source);
    }

    public void take(Source source) {
        if (!this.active) return;
        if (acceptCriteria != null && !acceptCriteria.test(source)) return;
        this.route(source);
    }

    @Override
    public void send(WorldlyPosition at, UUID sender, Holder<SoundEvent> soundHolder, float volume, float pitch, long seed) {
        this.send(at, sender, soundHolder, volume, pitch, 0, seed);
    }

    @Override
    public void send(WorldlyPosition at, UUID sender, Holder<SoundEvent> soundHolder, float volume, float pitch, float offset, long seed) {
        RadioSource newSource = new RadioSource(sender, at, soundHolder.value(), volume);
        newSource.pitch = pitch;
        newSource.offset = offset;
        newSource.seed = seed;
        newSource.activity = (float) (Math.clamp(0, 15, volume*15) * SimpleRadioLibrary.SERVER_CONFIG.router.activityRedstoneFactor);

        this.accept(newSource);
    }

    @Override
    public Source send(WorldlyPosition at, UUID sender, short[] data, float volume) {
        OpusEncoder encoder = this.getEncoder(sender);

        RadioSource newSource = new RadioSource(sender, at, encoder.encode(data), volume);
        newSource.activity = CommonRadioPlugin.analyzeActivity(data);

        this.accept(newSource);
        return newSource;
    }
    @Override
    public Source send(WorldlyPosition at, short[] data, float volume) {
        return this.send(at, this.reference, data, volume);
    }
    @Override
    public Source send(UUID sender, short[] data, float volume) {
        return this.send(this.getLocation(), sender, data, volume);
    }
    @Override
    public Source send(short[] data, float volume) {
        return this.send(this.getLocation(), this.reference, data, volume);
    }

    @Override
    public Source send(WorldlyPosition at, UUID sender, byte[] data, float volume) {
        OpusDecoder decoder = this.getDecoder(sender);

        RadioSource newSource = new RadioSource(sender, at, data, volume);
        newSource.activity = CommonRadioPlugin.analyzeActivity(decoder.decode(data));

        this.accept(newSource);
        return newSource;
    }
    @Override
    public Source send(WorldlyPosition at, byte[] data, float volume) {
        return this.send(at, this.reference, data, volume);
    }
    @Override
    public Source send(UUID sender, byte[] data, float volume) {
        return this.send(this.getLocation(), sender, data, volume);
    }
    @Override
    public Source send(byte[] data, float volume) {
        return this.send(this.getLocation(), this.reference, data, volume);
    }

    //this method is so dumb bro
    public void updateLocation(WorldlyPosition location) {
    }

    public void updateRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    public void tick(int tickCount) {
        // Calculate velocity and/or modify position/rotation for things like VS integration
        if (position != null) {
            this.updateRotation(CompatCore.modifyRotation(position, rotation));
            this.updateLocation(CompatCore.modifyPosition(position));

            Vector3f currentPosition = position.position();
            if (currentPosition != oldPosition) {
                currentPosition.sub(oldPosition, velocity);
            } else {
                velocity.set(0);
            }
            oldPosition.set(currentPosition);
        } else if (owner != null) {
            this.updateLocation(WorldlyPosition.of(owner.position().toVector3f(), owner.level()));
        }

        // Validate the connected routers
        // routers.removeIf(router -> !router.isValid());
        // ^ might cause problems if it's linked to the frequency router list; will continue later

        // Update router activity
        if (!this.active) {
            this.activity = 0;
            this.activityTime = -1;

            this.compiledSamples = 0;
            this.compiledActivity = 0;
        } else {
            if (this.activityTime > 0) {
                if (--this.activityTime == 0) {
                    this.activity = 0;
                    //informActivity();
                }
            } else if (this.activityTime == 0) {
                this.activityTime = -1;
            }
        }
    }

    public RadioSource prepareSource(RadioSource source, RadioRouter destination) {
        if (this.getLocation().equals(destination.getLocation())) return source;

        source.travel(this, destination, getFrequency());
        return source;
    }

    public boolean shouldRouteTo(RadioSource source, RadioRouter destination) {
        return true;
    }

    public void route(Source source, @Nullable Predicate<RadioRouter> criteria) {
        if (!this.active) return;
        RadioSource radioSource = (RadioSource) source;

        if (!radioSource.isValid()) {
            CommonSimpleRadio.warn("Invalid source; discarded");
            return;
        }

        if (routerAcceptor != null) {
            if (routerAcceptor.apply(radioSource))
                radioSource = radioSource.copy();
        }

        if (distributes) {
            if (this.distribute(radioSource)) radioSource = radioSource.copy();
        }

        for (int i = 0; i < routers.size(); i++) {
            RadioRouter router = (RadioRouter) routers.get(i);

            if (criteria != null) {
                if (!criteria.test(router)) continue;
            }
            if (!shouldRouteTo(radioSource, router)) continue;

            if (routeCriteria != null && !routeCriteria.test(radioSource, router)) continue;

            if (radioSource.willShort(router)) {
                router.shortCircuit();
                continue;
            }

            radioSource = this.prepareSource(radioSource, router);

            RadioSource oldSource = radioSource;
            if (i < routers.size()-1) radioSource = radioSource.copy();
            router.accept(oldSource);
        }
    }

    @Override
    public void route(Source source) {
        this.route(source, null);
    }

    public void compileActivity(Source source) {
        if (!this.active) return;

        if (source.getData() == null) {
            this.activity = source.getActivity();
            compiledActivity = 0;
            compiledSamples = 0;
        } else {
            compiledActivity += source.getActivity();
            if (compiledSamples++ >= SimpleRadioLibrary.SERVER_CONFIG.router.compileAmount) {
                this.activity = Math.sqrt(compiledActivity);
                compiledActivity = 0;
                compiledSamples = 0;
            }
        }

        if (activityTime < SimpleRadioLibrary.SERVER_CONFIG.router.activityForgiveness) {
            this.activityTime = SimpleRadioLibrary.SERVER_CONFIG.router.activityTime;
            informActivity();
        }
    }

    public void informActivity() {
        WorldlyPosition location = getLocation();
        if (!location.isClientSide()) {
            for (Player player : location.level.players()) {
                if (location.position().distance((float) player.getX(), (float) player.getY(), (float) player.getZ()) <= 100) {
                    Services.NETWORKING.sendToPlayer((ServerPlayer) player, new ClientboundActivityPacket(activity, this.getIdentifier()));
                }
            }
        }
    }

    @Override
    public void invalidate() {
        this.valid = false;
    }

    public boolean validate() {
        if (!valid) return false;

        if (owner == null) {
            if (position == null) {
                invalidate();
                return false;
            }

            boolean flag = true;
            if (this instanceof RadioSpeaker) {
                flag = Auricular.validateLocation(position, this.link != null ? this.link : Speaking.class, this.reference);
            } else if (this instanceof RadioListener) {
                flag = Auricular.validateLocation(position, this.link != null ? this.link : Listening.class, this.reference);
            } else if (this instanceof RadioReceiver) {
                flag = Frequencing.validateLocation(position, this.link != null ? this.link : Receiving.class, this.reference, null);
            } else if (this instanceof RadioTransmitter) {
                flag = Frequencing.validateLocation(position, this.link != null ? this.link : Transmitting.class, this.reference, null);
            } else {
                flag = this.link != null && RadioManager.getInstance().verifyLocationCollection(position, this.link);
            }

            if (!flag) {
                invalidate();
                return false;
            }
        } else {
            boolean isValid = RadioManager.getInstance().verifyEntityCollection(owner, stack -> {
                if (stack.isEmpty()) return false;

                if (!reference.equals(stack.get(REFERENCE))) return false;

                return true;
            });

            if (!isValid) {
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
