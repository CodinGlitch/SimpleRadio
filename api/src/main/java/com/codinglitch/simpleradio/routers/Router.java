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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Routes {@link Message}s to other routers.
 */
public interface Router {

    /**
     * Retrieves the reference that refers to this router. This is not necessarily
     * unique for each router, but will persist across reloads, and is the same across
     * the server-client boundary.
     * <br><br>
     * Routers that belong to the same purpose will share the same reference, but two
     * unrelated routers will have a different reference. That is, a transceiver can have
     * 4 different routers with the same reference differentiated by their class.
     *
     * @return The reference of this router
     */
    UUID getReference();

    /**
     * Retrieves the identifier that refers to this router. Identifiers are unique for <b>every</b>
     * router, but are ephemeral, so they do not persist across reloads. You should not rely on this to
     * be the same or stay the same.
     * <br><br>
     * For a short-lived {@link Message}, this is used to keep a record of every router it has visited,
     * to ensure that it cannot loop forever and cause a potential stack overflow or memory leak.
     *
     * @return The identifier of this router
     */
    short getIdentifier();

    @Nullable
    Frequency getFrequency();

    Boolean isClientSide();

    /**
     * Gets the effective location of this router. If position is provided, will use that,
     * otherwise will infer the position from the entity owning this router.
     */
    WorldlyPosition getLocation();

    /**
     * Tries to link another router to this one if it doesn't already exist.
     */
    Router tryAddRouter(Router router);

    /**
     * Links another router to this one.
     */
    Router addRouter(Router router);

    @Nullable
    WorldlyPosition getPosition();

    @Nullable
    Entity getOwner();
    Router getRouter(UUID id);
    Vec3 getConnectionPosition();
    Map<UUID, Queue<Message>> getPending();
    List<Router> getRouters();
    List<Wiring> getWires();

    boolean isActive();
    boolean isValid();
    Vec3 getConnectionOffset();
    Class<?> getLink();

    float getActivity();
    int getActivityTime();

    int getRedstoneMappedActivity();
    Quaternionf getRotation();

    /**
     * Marks that this router can distribute messages through wires.
     */
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

    /**
     * Takes the message, and pushes it if this router is capable of doing so.
     * @param message The message to be taken
     */
    void take(Message message);

    /**
     * Inserts a message into the buffer, where it will be extracted by the courier.
     * This method does not make any checks and will forcefully insert the message.
     * @param message The message to be inserted
     */
    void insert(Message message);

    /**
     * Pushes audio data into a separate buffer that dynamically splits it into the needed frame size.
     * This method may leave leftover samples if the total amount is not a multiple of the frame size.
     * @param audio The PCM data to be pushed
     */
    void push(short[] audio);

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
