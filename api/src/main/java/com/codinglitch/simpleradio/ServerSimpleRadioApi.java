package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.Listeners;
import com.codinglitch.simpleradio.core.Speakers;
import com.codinglitch.simpleradio.routers.Router;
import com.codinglitch.simpleradio.routers.RouterContainer;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
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

    public abstract Speakers speakers();
    public abstract Listeners listeners();

    public abstract boolean verifyLocationCollection(WorldlyPosition position, Class<?> clazz);
    public abstract boolean verifyEntityCollection(Entity entity, Predicate<ItemStack> itemCriteria);

    // ---- Routers ---- \\

    public abstract short getIdentifier(Predicate<Router> filter);

    public abstract List<Router> getRouters();

    public abstract Router removeRouter(Router router);
    public abstract Router removeRouter(Predicate<Router> criteria);
    public abstract Router removeRouter(short identifier);

    public abstract Router removeRouter(UUID uuid);
    public abstract Router removeRouter(Entity owner);
    public abstract Router removeRouter(WorldlyPosition location);

    public abstract Router getRouter(Predicate<Router> filter);
    public abstract Router getRouter(short identifier);

    public abstract Router getRouter(UUID reference, @Nullable String type);
    public abstract Router getRouter(UUID reference);
    public abstract Router getRouter(Entity owner);
    public abstract Router getRouter(WorldlyPosition location);

    // ---- Audio ---- \\

    public abstract void sendSound(WorldlyPosition location, SoundEvent soundEvent, float volume, float pitch, long seed);
    public abstract void sendSound(WorldlyPosition location, SoundEvent soundEvent, float volume, float pitch, float offset, long seed);
    public abstract void sendSound(WorldlyPosition location, String sound, float volume, float pitch, float offset, long seed);

    public abstract void sendAudio(WorldlyPosition location, UUID sender, byte[] data);

    public abstract void shortAt(WorldlyPosition location);
}
