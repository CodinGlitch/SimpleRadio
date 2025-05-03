package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.Listener;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
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

    private UnaryOperator<RadioSource> dataTransformer;
    private final Map<UUID, OpusDecoder> decoders;

    public float range = 8;

    public byte[] compiledData = new byte[] {};

    protected RadioListener(UUID reference) {
        super(reference);

        decoders = new HashMap<>();
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
        RadioManager.getInstance().registerRouterSided(this, isClient, null);
    }
    public RadioListener(WorldlyPosition location) {
        this(location, UUID.randomUUID());
    }
    public RadioListener(WorldlyPosition location, UUID uuid) {
        this(uuid);
        this.location = location;


        boolean isClient = location.isClientSide();
        RadioManager.getInstance().registerRouterSided(this, isClient, null);
    }

    @Override
    public float getRange() {
        return range;
    }

    @Override
    public void setRange(float range) {
        this.range = range;
    }

    public void transformer(UnaryOperator<RadioSource> transformer) {
        this.dataTransformer = transformer;
    }

    public OpusDecoder getDecoder(UUID sender) {
        return decoders.computeIfAbsent(sender, uuid -> CommonRadioPlugin.serverApi.createDecoder());
    }

    public void onData(byte[] data) {
        //TODO: compile like RadioSources into a larger sample
    }

    public void onSource(RadioSource source) {
        if (dataTransformer != null) {
            source = dataTransformer.apply(source);
        }

        this.compileActivity(source);

        source.delegate(this.reference);

        this.route(source);
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
    }
}
