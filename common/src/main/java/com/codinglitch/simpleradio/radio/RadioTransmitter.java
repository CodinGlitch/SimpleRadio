package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.api.central.FrequencingType;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioSource}s and transmits them along its connected {@link Frequency}.
 * <br>
 * <b>Does route further.</b>
 */
public class RadioTransmitter extends RadioRouter {
    public int antennaPower = 0;
    public Frequency frequency;

    public FrequencingType frequencingType;

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

    public RadioTransmitter transmitCriteria(BiPredicate<RadioSource, RadioRouter> criteria) {
        this.routeCriteria = criteria;
        return this;
    }

    public RadioTransmitter frequencingType(FrequencingType type) {
        this.frequencingType = type;
        return this;
    }

    public double getPower(Frequency.Modulation modulation) {
        int baseTransmissionPower = frequencingType.getTransmissionPower(modulation);

        return baseTransmissionPower + (antennaPower * frequencingType.antennaAptitude);
    }

    @Nullable
    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    @Override
    public boolean shouldRouteTo(RadioSource source, RadioRouter destination) {
        if (destination instanceof RadioReceiver receiver) {
            FrequencingType type = source.frequencingType == -1 ? this.frequencingType : source.getFrequencingType();
            double transmissionPower = source.frequencingType == -1 ? this.getPower(this.frequency.modulation) : source.transmissionPower;

            double distance = this.getLocation().distance(receiver.getLocation());
            double cost = distance * type.transmissionDiminishment;

            return (transmissionPower + receiver.getPower()) >= cost;
        }

        return super.shouldRouteTo(source, destination);
    }

    @Override
    public RadioSource prepareSource(RadioSource source, RadioRouter destination) {
        if (source.frequencingType == -1) {
            source.frequencingType = this.frequencingType.id;
            source.addPower(getPower(frequency.modulation));

            //CommonSimpleRadio.info("transmitting at {}", source.transmissionPower);
        }
        return super.prepareSource(source, destination);
    }

    @Override
    public void accept(RadioSource source) {
        if (!this.active) return;
        if (acceptCriteria != null && !acceptCriteria.test(source)) return;

        this.route(source, router -> {
            return source.owner == null || !source.owner.equals(router.id);
        });
    }
}
