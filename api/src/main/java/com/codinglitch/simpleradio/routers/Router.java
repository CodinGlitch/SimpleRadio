package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.Wiring;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.Message;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Routes {@link Message}s to other routers.
 */
public interface Router {
    UUID getReference();

    short getIdentifier();

    @Nullable
    Frequency getFrequency();

    Boolean isClientSide();

    WorldlyPosition getLocation();

    Router tryAddRouter(Router router);
    Router addRouter(Router router);

    @Nullable
    WorldlyPosition getPosition();

    @Nullable
    Entity getOwner();
    Router getRouter(UUID id);
    List<Router> getRouters();
    Vec3 getConnectionPosition();

    List<Wiring> getWires();

    boolean isActive();
    boolean isValid();
    Vec3 getConnectionOffset();
    Class<?> getLink();
    Vector3f getVelocity();
    float getActivity();

    int getActivityTime();

    int getRedstoneMappedActivity();
    Quaternionf getRotation();

    void allowDistribution();
    void setOwner(Entity owner);
    void setActive(boolean active);
    void setLink(Class<?> link);
    void setConnectionOffset(Vec3 connectionOffset);
    void setPosition(WorldlyPosition position);
    void setRotation(Quaternionf position);
    void setRoutingCriteria(BiPredicate<Message, Router> criteria);
    void setAcceptingCriteria(Predicate<Message> criteria);

    double distanceTo(Router other);


    void route(Message message);
    void accept(Message message);

    void send(WorldlyPosition at, UUID sender, Holder<SoundEvent> soundHolder, float volume, float pitch, long seed);
    void send(WorldlyPosition at, UUID sender, Holder<SoundEvent> soundHolder, float volume, float pitch, float offset, long seed);

    /**
     * Builds a {@link Message} and sends it to this router.
     * @param at The location to send it from.
     * @param sender The sender of the audio.
     * @param data The data to send, in raw PCM format.
     * @param volume The overall volume of the audio.
     */
    Message send(WorldlyPosition at, UUID sender, short[] data, float volume);
    Message send(WorldlyPosition at, short[] data, float volume);
    Message send(UUID sender, short[] data, float volume);
    Message send(short[] data, float volume);

    /**
     * Builds a {@link Message} and sends it to this router.
     * @param at The location to send it from.
     * @param sender The sender of the audio.
     * @param data The data to send, in Opus-encoded format.
     * @param volume The overall volume of the audio.
     */
    Message send(WorldlyPosition at, UUID sender, byte[] data, float volume);
    Message send(WorldlyPosition at, byte[] data, float volume);
    Message send(UUID sender, byte[] data, float volume);
    Message send(byte[] data, float volume);

    boolean validate();
    void invalidate();
}
