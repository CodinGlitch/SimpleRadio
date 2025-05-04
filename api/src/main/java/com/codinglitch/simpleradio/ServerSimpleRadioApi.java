package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.core.Listeners;
import com.codinglitch.simpleradio.core.Speakers;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class ServerSimpleRadioApi extends SimpleRadioApi {
    private static ServerSimpleRadioApi INSTANCE;
    public ServerSimpleRadioApi() {
        INSTANCE = this;
    }

    public static ServerSimpleRadioApi getInstance() {
        return INSTANCE;
    }

    public abstract Frequencies frequencies();

    public abstract Speakers speakers();
    public abstract Listeners listeners();

    // ---- Routers ---- \\

    public abstract List<Router> getRouters();

    public abstract void removeRouter(Router router);
    public abstract void removeRouter(Predicate<Router> criteria);
    public abstract void removeRouter(short identifier);

    public abstract void removeRouter(UUID uuid);
    public abstract void removeRouter(Entity owner);
    public abstract void removeRouter(WorldlyPosition location);

    public abstract Router getRouter(Predicate<Router> filter);
    public abstract Router getRouter(short identifier);

    public abstract Router getRouter(UUID reference, @Nullable String type);
    public abstract Router getRouter(UUID reference);
    public abstract Router getRouter(Entity owner);
    public abstract Router getRouter(WorldlyPosition location);

    public abstract void registerRouter(Router router);
    public abstract void registerRouter(Router router, @Nullable Frequency frequency);

    // ---- Audio ---- \\

    public abstract void sendSound(WorldlyPosition location, Holder<SoundEvent> soundHolder, float volume, float pitch, long seed);
    public abstract void sendSound(WorldlyPosition location, Holder<SoundEvent> soundHolder, float volume, float pitch, float offset, long seed);

    public abstract void sendAudio(WorldlyPosition location, UUID sender, byte[] data);

    public abstract void shortAt(WorldlyPosition location);
}
