package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.radio.FrequenciesImpl;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.radio.RadioTransmitter;
import com.codinglitch.simpleradio.routers.Receiver;
import com.codinglitch.simpleradio.routers.RouterContainer;
import com.codinglitch.simpleradio.routers.Transmitter;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static com.codinglitch.simpleradio.radio.FrequenciesImpl.DEFAULT_FREQUENCY;

public class FrequencyChannel implements Frequency {


    public boolean isValid = true;

    public final Modulation modulation;
    public final String frequency;

    public final RouterContainer<Receiver> receivers;
    public final RouterContainer<Transmitter> transmitters;

    public FrequencyChannel(String frequency, Modulation modulation) {
        Frequencies frequencies = RadioManager.getInstance().frequencies();
        if (!frequencies.check(frequency)) {
            CommonSimpleRadio.warn("{} does not follow frequency pattern! Replacing with default pattern {}", frequency, DEFAULT_FREQUENCY);
            frequency = DEFAULT_FREQUENCY;
        }

        this.frequency = frequency;
        this.modulation = modulation;
        this.receivers = new RouterContainer<>();
        this.transmitters = new RouterContainer<>();

        frequencies.add(this);
    }

    @Override
    public String getFrequency() {
        return this.frequency;
    }

    @Override
    public Modulation getModulation() {
        return this.modulation;
    }

    @Override
    public int getIndex() {
        return FrequenciesImpl.getFrequencyIndex(this.frequency, this.modulation);
    }

    @Override
    public List<Receiver> getReceivers() {
        return this.receivers.getContent();
    }

    @Override
    public List<Transmitter> getTransmitters() {
        return this.transmitters.getContent();
    }

    //---- Receivers ----\\


    @Override
    public Receiver getReceiver(Predicate<Receiver> filter) {
        Optional<Receiver> result = receivers.getContent().stream().filter(filter).findFirst();
        return result.orElse(null);
    }
    @Override
    public Receiver getReceiver(WorldlyPosition location) {
        return getReceiver(receiver -> location.equals(receiver.getLocation()));
    }
    @Override
    public Receiver getReceiver(Entity owner) {
        return getReceiver(receiver -> owner.equals(receiver.getOwner()));
    }
    @Override
    public Receiver getReceiver(UUID id) {
        return getReceiver(receiver -> id.equals(receiver.getReference()));
    }

    @Override
    public void registerReceiver(Receiver receiver) {
        RadioManager.getInstance().putRouter(receivers, (RadioReceiver) receiver);
    }

    @Override
    public Receiver addReceiver(Receiver receiver) {
        boolean isClient = false;
        if (receiver.getPosition() != null) isClient = receiver.getPosition().isClientSide();
        else if (receiver.getOwner() != null) isClient = receiver.getOwner().level().isClientSide;

        RadioManager.registerRouterSided(receiver, isClient, this);

        CommonSimpleRadio.debug("Added receiver {} to frequency {}", receiver.getReference(), this.frequency);
        return receiver;
    }

    @Override
    public Receiver tryAddReceiver(UUID id, WorldlyPosition location) {
        boolean isClient = location.isClientSide();

        Receiver receiver = null;//isClient ? ClientRadioManager.getReceiver(location) : getReceiver(location);
        if (receiver == null) receiver = getReceiver(id);

        if (receiver == null)
            return addReceiver(id, location);

        //CommonSimpleRadio.info("Failed to add receiver {} to frequency {} as they already exist", id, this.frequency);
        return receiver;
    }
    @Override
    public Receiver addReceiver(UUID id, WorldlyPosition location) {
        return addReceiver(new RadioReceiver(this, location, id));
    }

    @Override
    public Receiver tryAddReceiver(UUID id, Entity entity) {
        boolean isClient = entity.level().isClientSide;

        Receiver receiver = null;//isClient ? ClientRadioManager.getReceiver(entity) : getReceiver(entity);
        if (receiver == null) receiver = isClient ? ClientRadioManager.getInstance().getReceiver(id) : getReceiver(id);

        if (receiver == null)
            return addReceiver(id, entity);

        //CommonSimpleRadio.info("Failed to add receiver {} to frequency {} as they already exist", id, this.frequency);
        return receiver;
    }
    @Override
    public Receiver addReceiver(UUID id, Entity entity) {
        return addReceiver(new RadioReceiver(this, entity, id));
    }

    @Override
    public void removeReceiver(Predicate<Receiver> criteria) {
        receivers.removeIf(criteria);

        if (!this.validate()) RadioManager.getInstance().frequencies().remove(this);
    }
    @Override
    public void removeReceiver(Receiver receiver) {
        removeReceiver(receiver::equals);
    }
    @Override
    public void removeReceiver(Entity owner) {
        removeReceiver(receiver -> owner.equals(receiver.getOwner()));
    }
    @Override
    public void removeReceiver(WorldlyPosition location) {
        removeReceiver(receiver -> location.equals(receiver.getPosition()));
    }
    @Override
    public void removeReceiver(UUID id) {
        removeReceiver(receiver -> id.equals(receiver.getReference()));
    }

    //---- Transmitters ----\\

    @Override
    public Transmitter getTransmitter(Predicate<Transmitter> filter) {
        Optional<Transmitter> result = transmitters.getContent().stream().filter(filter).findFirst();
        return result.orElse(null);
    }
    @Override
    public Transmitter getTransmitter(WorldlyPosition location) {
        return getTransmitter(transmitter -> location.equals(transmitter.getPosition()));
    }
    @Override
    public Transmitter getTransmitter(Entity owner) {
        return getTransmitter(transmitter -> owner.equals(transmitter.getOwner()));
    }
    @Override
    public Transmitter getTransmitter(UUID id) {
        return getTransmitter(transmitter -> id.equals(transmitter.getReference()));
    }

    @Override
    public void registerTransmitter(Transmitter transmitter) {
        RadioManager.getInstance().putRouter(transmitters, transmitter);
    }

    @Override
    public Transmitter addTransmitter(Transmitter transmitter) {
        boolean isClient = false;
        if (transmitter.getPosition() != null) isClient = transmitter.getPosition().isClientSide();
        else if (transmitter.getOwner() != null) isClient = transmitter.getOwner().level().isClientSide;

        RadioManager.registerRouterSided(transmitter, isClient, this);

        CommonSimpleRadio.debug("Added transmitter {} to frequency {}", transmitter.getReference(), this.frequency);
        return transmitter;
    }

    @Override
    public Transmitter tryAddTransmitter(UUID id, WorldlyPosition location) {
        boolean isClient = location.isClientSide();

        Transmitter transmitter = null;//isClient ? ClientRadioManager.getTransmitter(location) : getTransmitter(location);
        if (transmitter == null) transmitter = getTransmitter(id);

        if (transmitter == null)
            return addTransmitter(id, location);

        //CommonSimpleRadio.info("Failed to add transmitter {} to frequency {} as they already exist", id, this.frequency);
        return transmitter;
    }
    @Override
    public Transmitter addTransmitter(UUID id, WorldlyPosition location) {
        return addTransmitter(new RadioTransmitter(this, location, id));
    }

    @Override
    public Transmitter tryAddTransmitter(UUID id, Entity entity) {
        boolean isClient = entity.level().isClientSide;

        Transmitter transmitter = null;//isClient ? ClientRadioManager.getTransmitter(entity) : getTransmitter(entity);
        if (transmitter == null) transmitter = isClient ? ClientRadioManager.getInstance().getTransmitter(id) : getTransmitter(id);

        if (transmitter == null)
            return addTransmitter(id, entity);

        //CommonSimpleRadio.info("Failed to add transmitter {} to frequency {} as they already exist", id, this.frequency);
        return transmitter;
    }
    @Override
    public Transmitter addTransmitter(UUID id, Entity entity) {
        return addTransmitter(new RadioTransmitter(this, entity, id));
    }

    @Override
    public void removeTransmitter(Predicate<Transmitter> criteria) {
        transmitters.removeIf(criteria);

        if (!this.validate()) RadioManager.getInstance().frequencies().remove(this);
    }
    @Override
    public void removeTransmitter(Transmitter transmitter) {
        removeTransmitter(transmitter::equals);
    }
    @Override
    public void removeTransmitter(Entity owner) {
        removeTransmitter(transmitter -> owner.equals(transmitter.getOwner()));
    }
    @Override
    public void removeTransmitter(WorldlyPosition location) {
        removeTransmitter(transmitter -> location.equals(transmitter.getPosition()));
    }
    @Override
    public void removeTransmitter(UUID id) {
        removeTransmitter(transmitter -> id.equals(transmitter.getReference()));
    }

    public void serverTick(int tickCount) {
        for (Transmitter transmitter : transmitters) {
            ((RadioTransmitter) transmitter).tick(tickCount);
        }
        for (Receiver receiver : receivers) {
            ((RadioReceiver) receiver).tick(tickCount);
        }

        // retired the 'pending' thing it was pretty stupid in hindsight
    }

    public boolean validate() {
        if (this.receivers.getContent().isEmpty() && this.transmitters.getContent().isEmpty()) {
            this.invalidate();
            return false;
        }

        return true;
    }

    public void invalidate() {
        this.isValid = false;
    }

    @Override
    public String toString() {
        return this.frequency + this.modulation.shorthand;
    }
}
