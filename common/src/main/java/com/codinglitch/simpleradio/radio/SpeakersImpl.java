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
    private final RouterContainer<Speaker> SPEAKERS = new RouterContainer<>();

    public void garbageCollect() {
        RadioManager.validate(SPEAKERS);
    }
    public void close() {
        SPEAKERS.clear();
    }

    @Override
    public List<Speaker> get() {
        return new ArrayList<>(SPEAKERS);
    }
    @Override
    public RouterContainer<Speaker> contents() {
        return SPEAKERS;
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
        if (speaker == null) speaker = isClient ? ClientRadioManager.INSTANCE.getSpeaker(id) : get(id);

        return speaker != null ? speaker : new RadioSpeaker(owner, id);
    }

    @Override
    public RadioSpeaker getOrCreate(Entity owner) { return getOrCreate(owner, null); }
    @Override
    public RadioSpeaker getOrCreate(WorldlyPosition location, @Nullable UUID id) {
        boolean isClient = location.level.isClientSide;

        RadioSpeaker speaker = null;//isClient ? ClientRadioManager.getSpeaker(location) : getSpeaker(location);
        if (speaker == null) speaker = isClient ? ClientRadioManager.INSTANCE.getSpeaker(id) : get(id);

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

        SPEAKERS.add(speaker);
        return (RadioSpeaker) speaker;
    }
}
