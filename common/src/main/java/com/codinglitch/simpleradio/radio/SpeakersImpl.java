package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.Speakers;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.routers.RouterContainer;
import com.codinglitch.simpleradio.routers.Speaker;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class SpeakersImpl implements Speakers {
    private static final RouterContainer<Speaker> SPEAKERS = new RouterContainer<>();

    public static void garbageCollect() {
        RadioManager.validate(SPEAKERS);
    }
    public static void close() {
        SPEAKERS.clear();
    }

    @Override
    public List<Speaker> get() {
        return new ArrayList<>(SPEAKERS);
    }

    @Override
    public void remove(Speaker speaker) {
        remove(speaker::equals);
    }
    @Override
    public void remove(Predicate<Speaker> criteria) {
        SPEAKERS.removeIf(criteria);
    }
    @Override
    public void remove(short identifier) {
        SPEAKERS.remove(identifier);
    }

    @Override
    public void remove(Entity owner) {
        remove(speaker -> owner.equals(speaker.getOwner()));
    }
    @Override
    public void remove(WorldlyPosition location) {
        remove(speaker -> location.equals(speaker.getPosition()));
    }
    @Override
    public void remove(UUID id) {
        remove(speaker -> id.equals(speaker.getReference()));
    }

    @Override
    public RadioSpeaker get(Entity owner) {
        return get(speaker -> owner.equals(speaker.getOwner()));
    }
    @Override
    public RadioSpeaker get(WorldlyPosition location) {
        return get(speaker -> location.equals(speaker.getPosition()));
    }
    @Override
    public RadioSpeaker get(UUID id) {
        return get(speaker -> id.equals(speaker.getReference()));
    }
    @Override
    public RadioSpeaker get(Predicate<Speaker> filter) {
        Optional<Speaker> result = SPEAKERS.stream().filter(filter).findFirst();
        return (RadioSpeaker) result.orElse(null);
    }

    @Override
    public RadioSpeaker getOrCreate(Entity owner, @Nullable UUID id) {
        boolean isClient = owner.level().isClientSide;

        RadioSpeaker speaker = null;//isClient ? ClientRadioManager.getSpeaker(owner) : getSpeaker(owner);
        if (speaker == null) speaker = isClient ? ClientRadioManager.getSpeaker(id) : get(id);

        return speaker != null ? speaker : new RadioSpeaker(owner, id);
    }

    @Override
    public RadioSpeaker getOrCreate(Entity owner) { return getOrCreate(owner, null); }
    @Override
    public RadioSpeaker getOrCreate(WorldlyPosition location, @Nullable UUID id) {
        boolean isClient = location.level.isClientSide;

        RadioSpeaker speaker = null;//isClient ? ClientRadioManager.getSpeaker(location) : getSpeaker(location);
        if (speaker == null) speaker = isClient ? ClientRadioManager.getSpeaker(id) : get(id);

        return speaker != null ? speaker : new RadioSpeaker(location, id);
    }
    @Override
    public RadioSpeaker getOrCreate(WorldlyPosition location) { return getOrCreate(location, null); }

    @Override
    public RadioSpeaker register(Speaker speaker) {
        if (speaker.getPosition() != null) {
            if (speaker.getPosition().isClientSide()) {
                CommonSimpleRadio.warn("Attempted to register a client-sided speaker on the server; cancelling");
                return null;
            }
        }

        RadioManager.getInstance().putRouter(SPEAKERS, speaker);
        return (RadioSpeaker) speaker;
    }
}
