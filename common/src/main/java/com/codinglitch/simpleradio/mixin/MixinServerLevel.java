package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level implements WorldGenLevel {


    protected MixinServerLevel(WritableLevelData $$0, ResourceKey<Level> $$1, Holder<DimensionType> $$2, Supplier<ProfilerFiller> $$3, boolean $$4, boolean $$5, long $$6, int $$7) {
        super($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
    }

    @Inject(at = @At("TAIL"), method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFJ)V")
    private void simpleradio$playSeededSound1_audioGathering(Player player, Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
        RadioManager.getInstance().onSoundPlayed((ServerLevel) (Object) this, entity.position(), sound, volume, pitch, seed);
    }

    @Inject(at = @At("TAIL"), method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFJ)V")
    private void simpleradio$playSeededSound2_audioGathering(Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
        RadioManager.getInstance().onSoundPlayed((ServerLevel) (Object) this, new Vec3(x, y, z), sound, volume, pitch, seed);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void simpleradio$tick_serverLevelTicking(BooleanSupplier supplier, CallbackInfo ci) {
        RadioManager.levelTick(this.getLevel());
    }
}