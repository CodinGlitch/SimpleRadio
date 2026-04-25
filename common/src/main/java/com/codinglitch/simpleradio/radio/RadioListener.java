package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.Listener;
import net.minecraft.world.entity.Entity;

import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioSource}s.
 * <br>
 * Often serves as the beginning of the audio pipeline.
 * <br>
 * <b>Does route further.</b>
 */
public class RadioListener extends RadioRouter implements Listener {

    private UnaryOperator<Source> dataTransformer;

    public float range = 8;

    public byte[] compiledData = new byte[] {};

    protected RadioListener(UUID reference) {
        super(reference);
    }
    protected RadioListener() {
        this(UUID.randomUUID());
    }

    public RadioListener(Entity owner) {
        this(owner, UUID.randomUUID());
    }
    public RadioListener(Entity owner, UUID uuid) {
        this(uuid);
        this.owner = owner;

        boolean isClient = owner.level().isClientSide();
        SimpleRadioApi.registerRouterSided(this, isClient, null);
    }
    public RadioListener(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioListener(WorldlyPosition location, UUID uuid) {
        this(uuid);
        this.position = location;

        boolean isClient = location.isClientSide();
        SimpleRadioApi.registerRouterSided(this, isClient, null);
    }

    @Override
    public float getRange() {
        return range;
    }

    @Override
    public void setRange(float range) {
        this.range = range;
    }

    @Override
    public void transformer(UnaryOperator<Source> transformer) {
        this.dataTransformer = transformer;
    }

    public void onData(byte[] data) {
        //TODO: compile like RadioSources into a larger sample
    }

    @Override
    public void listen(Source source) {
        if (dataTransformer != null) {
            source = dataTransformer.apply(source);
        }

        this.compileActivity(source);

        source.delegate(this.reference);

        CompatCore.acceptSource(this, source);
        this.route(source);
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
    }
}
