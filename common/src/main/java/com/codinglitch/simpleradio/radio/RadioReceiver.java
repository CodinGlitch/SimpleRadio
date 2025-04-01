package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.api.central.FrequencingType;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundActivityPacket;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

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
        this.setFrequency(frequency);
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

    public void setFrequency(Frequency frequency) {
        if (this.frequency != null) {
            this.frequency.removeReceiver(this);
        }

        this.frequency = frequency;
    }

    public RadioReceiver receiveCriteria(Predicate<RadioSource> criteria) {
        this.acceptCriteria = criteria;
        return this;
    }

    public RadioReceiver frequencingType(FrequencingType type) {
        this.frequencingType = type;
        return this;
    }

    public double getPower() {
        return frequencingType.receptionPower + (antennaPower * frequencingType.antennaAptitude);
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
    public void accept(RadioSource source) {
        //CommonSimpleRadio.info("receiving at {}", source.transmissionPower);

        if (!this.active) return;
        if (acceptCriteria != null && !acceptCriteria.test(source)) return;
        if (source.transmissionPower <= 0) return;

        this.trySendActivity();

        //super.accept(source);
        this.route(source);//, router -> !source.owner.equals(router.owner.getUUID()));
    }
}
