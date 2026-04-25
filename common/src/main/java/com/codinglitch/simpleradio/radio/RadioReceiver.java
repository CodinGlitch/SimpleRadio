package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.central.FrequencingType;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.Receiver;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioSource}s from its connected {@link Frequency}.
 * <br>
 * <b>Does route further.</b>
 */
public class RadioReceiver extends RadioRouter implements Receiver {
    public int antennaPower = 0;
    public Frequency frequency;

    public FrequencingType frequencingType;

    protected RadioReceiver(Frequency frequency, UUID id) {
        super(id);
        this.frequency(frequency);
    }
    protected RadioReceiver(Frequency frequency) {
        this(frequency, UUID.randomUUID());
    }

    public RadioReceiver(Frequency frequency, Entity owner) {
        this(frequency, owner, UUID.randomUUID());
    }
    public RadioReceiver(Frequency frequency, Entity owner, UUID uuid) {
        this(frequency, uuid);
        this.owner = owner;

        boolean isClient = owner.level().isClientSide();
        SimpleRadioApi.registerRouterSided(this, isClient, frequency);
    }
    public RadioReceiver(Frequency frequency, WorldlyPosition location) {
        this(frequency, location, UUID.randomUUID());
    }
    public RadioReceiver(Frequency frequency, WorldlyPosition location, UUID uuid) {
        this(frequency, uuid);
        this.position = location;
    }

    @Override
    public int getAntennaPower() {
        return antennaPower;
    }
    @Override
    public float getPower() {
        return frequencingType.receptionPower + (antennaPower * frequencingType.antennaAptitude);
    }
    @Override
    public FrequencingType getFrequencingType() {
        return frequencingType;
    }

    @Override
    public RadioReceiver frequency(Frequency frequency) {
        if (this.frequency != null) {
            this.frequency.removeReceiver(this);
        }

        this.frequency = frequency;
        return this;
    }

    @Override
    public RadioReceiver frequencingType(FrequencingType type) {
        this.frequencingType = type;
        return this;
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
    }

    @Nullable
    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    @Override
    public void take(Source source) {
        //CommonSimpleRadio.info("receiving at {}", source.transmissionPower);

        if (!this.active) return;
        if (acceptCriteria != null && !acceptCriteria.test(source)) return;
        if (source.getPower() <= 0) return;

        this.compileActivity(source);

        //super.accept(source);
        this.route(source);//, router -> !source.owner.equals(router.owner.getUUID()));
    }
}
