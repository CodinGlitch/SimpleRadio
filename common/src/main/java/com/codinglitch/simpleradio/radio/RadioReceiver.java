package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.Receiving;
import com.codinglitch.simpleradio.core.central.Transmitting;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class RadioReceiver extends RadioRouter {
    public Frequency frequency;

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

    @Nullable
    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    @Override
    public void accept(RadioSource source) {
        if (source.transmissionPower <= 0) {
            return;
        }

        super.accept(source);
        this.route(source);//, router -> !source.owner.equals(router.owner.getUUID()));
    }
}
