package com.codinglitch.simpleradio.core.registry.entities;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.api.central.Medium;
import com.codinglitch.simpleradio.api.central.Socket;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundWireEffectPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioHeader;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioRouter;
import com.codinglitch.simpleradio.radio.RadioSource;
import net.minecraft.core.particles.ParticleTypes;
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

import java.util.*;

public class Wire extends Entity implements Medium {
    private static final EntityDataAccessor<Optional<UUID>> FROM = SynchedEntityData.defineId(Wire.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> FROM_TYPE = SynchedEntityData.defineId(Wire.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Optional<UUID>> TO = SynchedEntityData.defineId(Wire.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> TO_TYPE = SynchedEntityData.defineId(Wire.class, EntityDataSerializers.STRING);

    private int killTime = -1;


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

    /**
     * Connect two given {@link Socket}s within a level.
     * @param from The first socket
     * @param to The second socket
     * @param level The level they reside in
     * @return The resulting wire
     */
    public static Wire connect(Socket from, Socket to, Level level) {
        if (from.equals(to)) return null;

        RadioRouter fromRouter = from.getRouter();
        if (fromRouter == null) return null;

        RadioRouter toRouter = to.getRouter();
        if (toRouter == null) return null;

        Wire wire = new Wire(level);
        wire.moveTo(new Vec3(fromRouter.location));
        wire.setFrom(from.getID(), RadioRouter.Type.byInstance(fromRouter));
        wire.setTo(to.getID(), RadioRouter.Type.byInstance(toRouter));

        if (from.hasWire(wire)) {
            wire.kill();
            return null;
        }

        if (to.hasWire(wire)) {
            wire.kill();
            return null;
        }

        level.addFreshEntity(wire);

        from.connect(wire);
        to.connect(wire);

        return wire;
    }

    /**
     * Get the router opposite to the one provided.
     * @param source The originating router
     */
    public RadioRouter transport(RadioRouter source) {
        RadioRouter from = this.getFromRouter();
        RadioRouter to = this.getToRouter();

        if (source == from) return to;
        if (source == to) return from;

        return null;
    }

    /**
     * Relay a {@link RadioSource} along this wire.
     * @param source The {@link RadioSource} to relay
     * @param originSocket The {@link Socket} the source came from
     */
    public void relay(RadioSource source, Socket originSocket) {
        UUID fromID = this.getFrom().orElse(null);
        UUID toID = this.getTo().orElse(null);

        RadioRouter.Type fromType = this.getFromType();
        RadioRouter.Type toType = this.getToType();
        if (fromID == null || toID == null) {
            CommonSimpleRadio.warn("Relaying cancelled; invalid wire [{}] to relay across.", this.getUUID());
            return;
        }

        RadioRouter from = RadioRouter.getRouterFromUUID(fromID, fromType);
        RadioRouter to = RadioRouter.getRouterFromUUID(toID, toType);
        if (from == null || to == null) {
            CommonSimpleRadio.warn("Relaying cancelled; either end was unable to be found.");
            return;
        }

        Level level = this.level();
        boolean isReversed = originSocket.getID().equals(toID);
        RadioRouter destination = isReversed ? from : to;

        if (!level.isClientSide() && !effectCooldowns.containsKey(source.owner) && SimpleRadioLibrary.SERVER_CONFIG.wire.effectInterval != -1) {
            for (Player player : level.players()) {
                if (player.distanceTo(this) <= 100) {
                    Services.NETWORKING.sendToPlayer((ServerPlayer) player, new ClientboundWireEffectPacket(this.getId(), isReversed));
                }
            }

            this.effectCooldowns.put(source.owner, SimpleRadioLibrary.SERVER_CONFIG.wire.effectInterval);
        }

        if (source instanceof RadioHeader header) {
            if (header.willShort(this)) {
                originSocket.shortCircuit();
            } else {
                header.visit(this);
            }
        }

        source.travel(from, to, this);

        if (SimpleRadioLibrary.SERVER_CONFIG.wire.transmissionTime == -1) {
            destination.accept(source);
        } else {
            RadioManager.queueSource(source, destination, (int) Math.round(SimpleRadioLibrary.SERVER_CONFIG.wire.transmissionTime * this.getLength()));
        }
    }

    public float getLength() {
        RadioRouter from = this.getFromRouter();
        if (from == null) return 0;

        RadioRouter to = this.getToRouter();
        if (to == null) return 0;

        return from.location.distance(to.location);
    }

    @Nullable
    public RadioRouter getFromRouter() {
        UUID fromID = this.getFrom().orElse(null);
        if (fromID == null) return null;

        return this.level().isClientSide ? ClientRadioManager.getRouter(fromID) : RadioRouter.getRouterFromUUID(fromID, this.getFromType());
    }
    public Optional<UUID> getFrom() {
        return this.getEntityData().get(FROM);
    }
    public RadioRouter.Type getFromType() {
        return RadioRouter.Type.byName(this.getEntityData().get(FROM_TYPE));
    }
    public void setFrom(UUID to, RadioRouter.Type type) {
        this.getEntityData().set(FROM, Optional.of(to));
        if (type != null)
            this.getEntityData().set(FROM_TYPE, type.name());
    }

    @Nullable
    public RadioRouter getToRouter() {
        UUID toID = this.getTo().orElse(null);
        if (toID == null) return null;

        return this.level().isClientSide ? ClientRadioManager.getRouter(toID) : RadioRouter.getRouterFromUUID(toID, this.getToType());
    }
    public Optional<UUID> getTo() {
        return this.getEntityData().get(TO);
    }
    public RadioRouter.Type getToType() {
        return RadioRouter.Type.byName(this.getEntityData().get(TO_TYPE));
    }
    public void setTo(UUID to, RadioRouter.Type type) {
        this.getEntityData().set(TO, Optional.of(to));
        if (type != null)
            this.getEntityData().set(TO_TYPE, type.name());
    }

    public void shortCircuit() {
        this.kill();
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

        if (this.level().isClientSide) {
            int effectDuration = (int) Math.round(SimpleRadioLibrary.CLIENT_CONFIG.wire.effectTime * this.getLength());

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
        } else {
            UUID fromUUID = this.getFrom().orElse(null);
            RadioRouter.Type fromType = this.getFromType();

            UUID toUUID = this.getTo().orElse(null);
            RadioRouter.Type toType = this.getToType();

            if (fromUUID != null && toUUID != null) {
                if (fromUUID == toUUID) {
                    this.kill();
                    return;
                }

                RadioRouter from = RadioRouter.getRouterFromUUID(fromUUID, fromType);
                RadioRouter to = RadioRouter.getRouterFromUUID(toUUID, toType);

                if (from == null) {
                    if (to != null) this.moveTo(new Vec3(to.getLocation().position()));
                    this.tickDeath();
                    return;
                }

                if (to == null) {
                    this.moveTo(new Vec3(from.getLocation().position()));
                    this.tickDeath();
                    return;
                }

                if (from.location.position().distance(to.location.position()) > SimpleRadioLibrary.SERVER_CONFIG.wire.range) {
                    this.tickDeath();
                    return;
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
        ItemEntity drop = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(SimpleRadioItems.COPPER_WIRE, 1));
        this.level().addFreshEntity(drop);

        RadioRouter from = this.getFromRouter();
        if (from != null) {
            from.disconnect(this);
        }

        RadioRouter to = this.getToRouter();
        if (to != null) {
            to.disconnect(this);
        }

        super.remove(reason);
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
    protected void defineSynchedData() {
        this.getEntityData().define(FROM, Optional.empty());
        this.getEntityData().define(FROM_TYPE, "");

        this.getEntityData().define(TO, Optional.empty());
        this.getEntityData().define(TO_TYPE, "");
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
