package com.codinglitch.simpleradio.core.registry.entities;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.central.Wiring;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundWireEffectPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioRouter;
import com.codinglitch.simpleradio.radio.Source;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Wire extends Entity implements Wiring {
    private static final EntityDataAccessor<Optional<UUID>> FROM = SynchedEntityData.defineId(Wire.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> FROM_TYPE = SynchedEntityData.defineId(Wire.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Optional<UUID>> TO = SynchedEntityData.defineId(Wire.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> TO_TYPE = SynchedEntityData.defineId(Wire.class, EntityDataSerializers.STRING);

    private int killTime = -1;

    private int deathRowTime = -1;
    private float deathRowPosition = -1;


    private HashMap<UUID, Integer> effectCooldowns = new HashMap<>();
    public final ArrayList<Effect> effectList = new ArrayList<>();

    public static class Effect {
        public int direction = 0;
        public int progress = 0;
    }

    public Wire(EntityType<?> entityType, Level level) {
        super(entityType, level);

        this.noCulling = true;
    }

    public Wire(Level level) {
        this(SimpleRadioEntities.WIRE, level);
    }

    @Override
    public UUID getReference() {
        return this.uuid;
    }

    /**
     * Connect two given {@link Socket}s within a level.
     * @param from The first socket
     * @param to The second socket
     * @param level The level they reside in
     * @return The resulting wire
     */
    public static Wire connect(Socket from, Socket to, Level level) {
        if (from.equals(to)) return null;

        Router fromRouter = from.getRouter();
        if (fromRouter == null) return null;

        Router toRouter = to.getRouter();
        if (toRouter == null) return null;

        UUID fromRef = fromRouter.getReference();
        UUID toRef = toRouter.getReference();

        if (from.hasWire(fromRef, toRef)) return null;
        if (to.hasWire(fromRef, toRef)) return null;

        Wire wire = new Wire(level);
        wire.moveTo(new Vec3(fromRouter.getLocation()));
        wire.setFrom(fromRouter);
        wire.setTo(toRouter);

        level.addFreshEntity(wire);

        from.connect(wire);
        to.connect(wire);

        return wire;
    }

    @Override
    public RadioRouter transport(Router source) {
        RadioRouter from = this.getFromRouter();
        RadioRouter to = this.getToRouter();

        if (source == from) return to;
        if (source == to) return from;

        return null;
    }

    @Override
    public void relay(Source source, Socket originSocket) {
        if (!this.isValid()) return;

        UUID fromRef = this.getFrom().orElse(null);
        UUID toRef = this.getTo().orElse(null);

        String fromType = this.getFromType();
        String toType = this.getToType();
        if (fromRef == null || toRef == null) {
            CommonSimpleRadio.warn("Relaying cancelled; invalid wire [{}] to relay across.", this.getReference());
            return;
        }

        RadioRouter from = (RadioRouter) SimpleRadioApi.getRouterSided(fromRef, fromType, this.level().isClientSide());
        RadioRouter to = (RadioRouter) SimpleRadioApi.getRouterSided(toRef, toType, this.level().isClientSide());
        if (from == null || to == null) {
            CommonSimpleRadio.warn("Relaying cancelled; either end was unable to be found.");
            return;
        }

        Level level = this.level();
        boolean isReversed = originSocket.getReference().equals(toRef);
        RadioRouter origin = isReversed ? to : from;
        RadioRouter destination = isReversed ? from : to;

        // Short circuit a socket if we have visited it previously
        if (source.willShort(destination)) {
            destination.shortCircuit();
            return;
        }

        // Short circuit a wire if two sources are colliding on it
        if (SimpleRadioLibrary.SERVER_CONFIG.wire.transmissionTime != -1) {
            AtomicInteger timeUntilDemise = new AtomicInteger();
            AtomicReference<Float> placeOfDemise = new AtomicReference<>((float) 0);
            if (RadioManager.getInstance().readQueue(queued -> {
                if (queued.source.getWireMedium().equals(this) && queued.router.equals(origin)) {
                    int maxProgress = Math.round(SimpleRadioLibrary.SERVER_CONFIG.wire.transmissionTime * this.getLength());
                    float progress = (float) queued.time / maxProgress;

                    timeUntilDemise.set((int) Math.ceil((float)queued.time / 2f));
                    if (isReversed) {
                        placeOfDemise.set(1 - progress);
                    } else {
                        placeOfDemise.set(progress);
                    }
                    return true;
                }
                return false;
            })) {
                this.queueDemise(timeUntilDemise.get(), placeOfDemise.get());
            }
        }

        if (!level.isClientSide() && !effectCooldowns.containsKey(source.getOwner()) && SimpleRadioLibrary.SERVER_CONFIG.wire.effectInterval != -1) {
            for (Player player : level.players()) {
                if (player.distanceTo(this) <= 100) {
                    Services.NETWORKING.sendToPlayer((ServerPlayer) player, new ClientboundWireEffectPacket(this.getId(), isReversed));
                }
            }

            this.effectCooldowns.put(source.getOwner(), SimpleRadioLibrary.SERVER_CONFIG.wire.effectInterval);
        }

        //CommonSimpleRadio.info("Relaying from {} to {}", origin, destination);

        source.travel(from, to, this);

        if (SimpleRadioLibrary.SERVER_CONFIG.wire.transmissionTime <= 0) {
            destination.accept(source);
        } else {
            RadioManager.getInstance().queueSource(source, destination, Math.round(SimpleRadioLibrary.SERVER_CONFIG.wire.transmissionTime * this.getLength()));
        }
    }

    public float getLength() {
        RadioRouter from = this.getFromRouter();
        if (from == null) return 0;

        RadioRouter to = this.getToRouter();
        if (to == null) return 0;

        return from.getLocation().position().distance(to.getLocation().position());
    }

    @Nullable
    public RadioRouter getFromRouter() {
        UUID reference = this.getFrom().orElse(null);
        if (reference == null) return null;

        return (RadioRouter) RadioManager.getRouterSided(reference, this.getFromType(), this.level().isClientSide);
    }
    public Optional<UUID> getFrom() {
        return this.getEntityData().get(FROM);
    }

    @Nullable
    public String getFromType() {
        String type = this.getEntityData().get(FROM_TYPE);
        return type.isEmpty() ? null : type;
    }
    @Override
    public void setFrom(Router from) {
        this.getEntityData().set(FROM, Optional.of(from.getReference()));
        if (from.getClass() != RadioRouter.class)
            this.getEntityData().set(FROM_TYPE, from.getClass().getSimpleName());
    }

    @Override
    @Nullable
    public RadioRouter getToRouter() {
        UUID reference = this.getTo().orElse(null);
        if (reference == null) return null;

        return (RadioRouter) RadioManager.getRouterSided(reference, this.getToType(), this.level().isClientSide);
    }
    @Override
    public Optional<UUID> getTo() {
        return this.getEntityData().get(TO);
    }
    @Override
    @Nullable
    public String getToType() {
        String type = this.getEntityData().get(TO_TYPE);
        return type.isEmpty() ? null : type;
    }
    @Override
    public void setTo(Router to) {
        this.getEntityData().set(TO, Optional.of(to.getReference()));
        if (to.getClass() != RadioRouter.class)
            this.getEntityData().set(TO_TYPE, to.getClass().getSimpleName());
    }

    @Override
    public boolean isValid() {
        return this.isAlive();
    }

    @Override
    public void burnOut(RemovalReason reason) {
        RadioManager.getInstance().dequeueSource(queuedSource -> queuedSource.source.getWireMedium() == this);
        this.remove(reason);
    }

    @Override
    public void burnOut() {
        this.burnOut(RemovalReason.KILLED);
    }

    @Override
    public void shortCircuit(Vector3f at) {
        if (level() instanceof ServerLevel level) RadioManager.getInstance().shortAt(WorldlyPosition.of(at, level));
        this.burnOut();
    }
    public void shortCircuit() {
        this.shortCircuit(this.position().toVector3f());
    }

    public void queueDemise(int time, float position) {
        this.deathRowTime = time;
        this.deathRowPosition = position;
    }

    private void tickDeath() {
        //this.setInvisible(true);

        if (this.killTime == 0) {
            this.kill();
        } else if (this.killTime == -1) {
            this.killTime = SimpleRadioLibrary.SERVER_CONFIG.wire.invalidDeathTime;
        } else {
            this.killTime--;
        }
    }

    @Override
    public void tick() {
        if (!this.level().isLoaded(this.blockPosition())) return;

        this.noPhysics = true;

        this.effectCooldowns.replaceAll((owner, time) -> time - 1);
        this.effectCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);

        UUID fromRef = this.getFrom().orElse(null);
        UUID toRef = this.getTo().orElse(null);

        if (this.level().isClientSide) {
            int effectDuration = Math.round(SimpleRadioLibrary.CLIENT_CONFIG.wire.effectTime * this.getLength());

            Iterator<Effect> iterator = this.effectList.iterator();
            while (iterator.hasNext()) {
                Effect effect = iterator.next();

                if (effect.direction == -1) {
                    if (effect.progress < 0) iterator.remove();
                } else {
                    if (effect.progress > effectDuration) iterator.remove();
                }

                effect.progress += effect.direction;
            }

            if (fromRef != null && toRef != null) {
                RadioRouter from = (RadioRouter) ClientRadioManager.getInstance().getRouter(fromRef);
                RadioRouter to = (RadioRouter) ClientRadioManager.getInstance().getRouter(toRef);

                if (from != null && !from.hasWire(this)) from.connect(this);
                if (to != null && !to.hasWire(this)) to.connect(this);
            }
        } else {
            String fromType = this.getFromType();
            String toType = this.getToType();

            if (fromRef != null && toRef != null) {
                if (fromRef == toRef) {
                    this.kill();
                    return;
                }
                RadioRouter from = (RadioRouter) RadioManager.getInstance().getRouter(fromRef, fromType);
                RadioRouter to = (RadioRouter) RadioManager.getInstance().getRouter(toRef, toType);

                if (from == null) {
                    CommonSimpleRadio.debug("Ticking wire death via missing FROM");
                    if (to != null) this.moveTo(new Vec3(to.getLocation().position()));
                    this.tickDeath();
                    return;
                }

                if (to == null) {
                    CommonSimpleRadio.debug("Ticking wire death via missing TO");
                    this.moveTo(new Vec3(from.getLocation().position()));
                    this.tickDeath();
                    return;
                }

                if (from.getLocation().position().distance(to.getLocation().position()) > SimpleRadioLibrary.SERVER_CONFIG.wire.range) {
                    CommonSimpleRadio.debug("Ticking wire death via distance");
                    this.tickDeath();
                    return;
                }

                if (deathRowTime != -1) {
                    if (deathRowTime-- == 0) {
                        Vector3f position = from.getLocation().position().lerp(to.getLocation().position(), deathRowPosition);

                        this.shortCircuit(position);
                        return;
                    }
                }

                //from.tryAddRouter(to);
                //to.tryAddRouter(from);

                if (!from.hasWire(this)) {
                    from.connect(this);
                }

                if (!to.hasWire(this)) {
                    to.connect(this);
                }

                //this.moveTo(new Vec3(from.getLocation().position()));

                this.killTime = -1;
            } else {
                this.tickDeath();
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!reason.shouldDestroy()) {
            super.remove(reason);
            return;
        }

        ItemEntity drop = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(SimpleRadioItems.COPPER_WIRE, 1));
        this.level().addFreshEntity(drop);
        cleanUp();

        super.remove(reason);
    }

    @Override
    public void onClientRemoval() {
        cleanUp();
        super.onClientRemoval();
    }

    public void cleanUp() {
        RadioRouter from = this.getFromRouter();
        if (from != null) {
            from.disconnect(this);
        }

        RadioRouter to = this.getToRouter();
        if (to != null) {
            to.disconnect(this);
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean shouldRender(double $$0, double $$1, double $$2) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FROM, Optional.empty());
        builder.define(FROM_TYPE, "");

        builder.define(TO, Optional.empty());
        builder.define(TO_TYPE, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("from") && compoundTag.contains("to")) {
            this.getEntityData().set(FROM, Optional.of(compoundTag.getUUID("from")));
            this.getEntityData().set(FROM_TYPE, compoundTag.getString("fromType"));

            this.getEntityData().set(TO, Optional.of(compoundTag.getUUID("to")));
            this.getEntityData().set(TO_TYPE, compoundTag.getString("toType"));
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        this.getEntityData().get(FROM).ifPresent(uuid -> {
            compoundTag.putUUID("from", uuid);
            compoundTag.putString("fromType", this.getEntityData().get(FROM_TYPE));
        });

        this.getEntityData().get(TO).ifPresent(uuid -> {
            compoundTag.putUUID("to", uuid);
            compoundTag.putString("toType", this.getEntityData().get(TO_TYPE));
        });
    }
}
