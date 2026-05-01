package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.FrequencingType;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.Transmitter;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioSource}s and transmits them along its connected {@link Frequency}.
 * <br>
 * <b>Does route further.</b>
 */
public class RadioTransmitter extends RadioRouter implements Transmitter {
    public int antennaPower = 0;
    public Frequency frequency;

    public FrequencingType frequencingType;

    protected RadioTransmitter(Frequency frequency, UUID id) {
        super(id);
        this.frequency(frequency);
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
        this.position = location;
    }

    @Override
    public int getAntennaPower() {
        return antennaPower;
    }
    @Override
    public float getPower(Frequency.Modulation modulation) {
        int baseTransmissionPower = frequencingType.getTransmissionPower(modulation);

        return baseTransmissionPower + (antennaPower * frequencingType.antennaAptitude);
    }
    @Override
    public FrequencingType getFrequencingType() {
        return frequencingType;
    }

    @Override
    public RadioTransmitter frequency(Frequency frequency) {
        CommonSimpleRadio.info(frequency);

        if (this.frequency != null) {
            this.frequency.removeTransmitter(this);
        }

        this.frequency = frequency;
        this.routers = (List) this.frequency.getReceivers();
        return this;
    }

    public RadioTransmitter frequencingType(FrequencingType type) {
        this.frequencingType = type;
        return this;
    }

    @Nullable
    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    @Override
    public boolean shouldRouteTo(RadioSource source, RadioRouter destination) {
        if (destination instanceof RadioReceiver receiver) {
            if (source.willShort(receiver)) return false;

            FrequencingType type = source.frequencingType == -1 ? this.frequencingType : source.getFrequencingType();
            double transmissionPower = source.frequencingType == -1 ? this.getPower(this.frequency.getModulation()) : source.transmissionPower;

            double distance = this.getLocation().distance(receiver.getLocation());
            double cost = distance * type.transmissionDiminishment;

            return (transmissionPower + receiver.getPower()) >= cost;
        }

        return super.shouldRouteTo(source, destination);
    }

    @Override
    public RadioSource prepareSource(RadioSource source, RadioRouter destination) {
        if (source.frequencingType == -1) {
            float transmissionPower = getPower(frequency.getModulation());

            source.frequencingType = this.frequencingType.id;
            source.transmissionCap = transmissionPower;
            source.addPower(transmissionPower);

            //CommonSimpleRadio.info("transmitting at {}", source.transmissionPower);
        }
        return super.prepareSource(source, destination);
    }

    @Override
    public void take(Source source) {
        if (!this.active) return;
        if (acceptCriteria != null && !acceptCriteria.test(source)) return;

        this.route(source, router -> {
            return source.getOwner() == null || !source.getOwner().equals(router.reference);
        });
    }
}
