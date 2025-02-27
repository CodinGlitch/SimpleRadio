package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.FrequencingType;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioSource}s from its connected {@link Frequency}.
 * <br>
 * <b>Does route further.</b>
 */
public class RadioReceiver extends RadioRouter {
    public int antennaPower = 0;
    public Frequency frequency;

    public FrequencingType frequencingType;

    protected RadioReceiver(Frequency frequency, UUID id) {
        super(id);
        this.frequency = frequency;
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
    }
    public RadioReceiver(Frequency frequency, WorldlyPosition location) {
        this(frequency, location, UUID.randomUUID());
    }
    public RadioReceiver(Frequency frequency, WorldlyPosition location, UUID uuid) {
        this(frequency, uuid);
        this.location = location;
    }

    public RadioReceiver frequencingType(FrequencingType type) {
        this.frequencingType = type;
        return this;
    }

    public double getPower() {
        return frequencingType.receptionPower + (antennaPower * frequencingType.antennaAptitude);
    }

    @Nullable
    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    @Override
    public void accept(RadioSource source) {
        //CommonSimpleRadio.info("receiving at {}", source.transmissionPower);

        if (source.transmissionPower <= 0) return;

        //super.accept(source);
        this.route(source);//, router -> !source.owner.equals(router.owner.getUUID()));
    }
}
