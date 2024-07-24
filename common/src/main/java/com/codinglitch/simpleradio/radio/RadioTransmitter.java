package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

public class RadioTransmitter extends RadioRouter {
    public BiPredicate<RadioSource, RadioRouter> transmitCriteria;

    public static RadioTransmitter getOrCreateTransmitter(Frequency frequency, Entity owner, @Nullable UUID id) {
        RadioTransmitter transmitter = frequency.getTransmitter(owner);
        if (transmitter == null) transmitter = frequency.getTransmitter(id);

        return transmitter != null ? transmitter : new RadioTransmitter(frequency, owner);
    }
    public static RadioTransmitter getOrCreateTransmitter(Frequency frequency, Entity owner) { return getOrCreateTransmitter(frequency, owner, null); }

    public static RadioTransmitter getOrCreateTransmitter(Frequency frequency, WorldlyPosition location, @Nullable UUID id) {
        RadioTransmitter transmitter = frequency.getTransmitter(location);
        if (transmitter == null) transmitter = frequency.getTransmitter(id);

        return transmitter != null ? transmitter : new RadioTransmitter(frequency);
    }
    public static RadioTransmitter getOrCreateTransmitter(Frequency frequency, WorldlyPosition location) { return getOrCreateTransmitter(frequency, location, null); }

    public Frequency frequency;

    protected RadioTransmitter(Frequency frequency, UUID id) {
        super(id);
        this.setFrequency(frequency);
    }
    protected RadioTransmitter(Frequency frequency) {
        this(frequency, UUID.randomUUID());
    }

    public RadioTransmitter(Frequency frequency, Entity owner) {
        this(frequency, owner, UUID.randomUUID());
    }
    public RadioTransmitter(Frequency frequency, Entity owner, UUID uuid) {
        this(frequency, uuid);
        this.owner = owner;
    }
    public RadioTransmitter(Frequency frequency, WorldlyPosition location) {
        this(frequency, location, UUID.randomUUID());
    }
    public RadioTransmitter(Frequency frequency, WorldlyPosition location, UUID uuid) {
        this(frequency, uuid);
        this.location = location;
    }

    public void setFrequency(Frequency frequency) {
        if (this.frequency != null) {
            this.frequency.removeTransmitter(this);
        }

        this.frequency = frequency;
        this.routers = (List<RadioRouter>)(List<?>) this.frequency.receivers;
    }

    public void transmitCriteria(BiPredicate<RadioSource, RadioRouter> criteria) {
        this.transmitCriteria = criteria;
    }

    @Nullable
    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    @Override
    public RadioSource prepareSource(RadioSource source, RadioRouter router) {
        if (source.type == null) {
            source.type = RadioSource.Type.TRANSMITTER;
            source.addPower(source.getTransmissionPower(frequency.modulation));
        }
        return super.prepareSource(source, router);
    }

    @Override
    public void accept(RadioSource source) {
        super.accept(source);
        this.route(source, router -> {
            if (transmitCriteria != null && !transmitCriteria.test(source, router)) {
                return false;
            }

            return !source.owner.equals(router.id);
        });
    }
}
