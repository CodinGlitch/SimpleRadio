package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * A type of {@link RadioRouter} that accepts {@link RadioSource}s.
 * <br>
 * Often serves as the beginning of the audio pipeline.
 * <br>
 * <b>Does route further.</b>
 */
public class RadioListener extends RadioRouter {

    private UnaryOperator<RadioSource> dataTransformer;
    private static final Queue<RadioSource> pendingSources = new LinkedList<>();

    public float range = 8;
    public long lastHeader = 0;

    protected RadioListener(UUID id) {
        super(id);

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
        RadioManager.registerRouterSided(this, isClient, null);
    }
    public RadioListener(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioListener(WorldlyPosition location, UUID uuid) {
        this(uuid);
        this.location = location;


        boolean isClient = location.isClientSide();
        RadioManager.registerRouterSided(this, isClient, null);
    }

    public void setRange(float range) {
        this.range = range;
    }

    public void tryRouteHeader() {
        if (this.location == null) return;

        long currentTime = this.location.level.getGameTime();
        if (currentTime - lastHeader < SimpleRadioLibrary.SERVER_CONFIG.wire.headerInterval) return;

        //RadioHeader header = new RadioHeader(this.location);
        //this.route(header);

        this.lastHeader = currentTime;
    }

    public void transformer(UnaryOperator<RadioSource> transformer) {
        this.dataTransformer = transformer;
    }

    public void onData(RadioSource source) {
        if (dataTransformer != null) {
            source = dataTransformer.apply(source);
        }

        this.trySendActivity();

        source.delegate(this.reference);

        this.tryRouteHeader();
        this.route(source);
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
    }
}
