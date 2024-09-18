package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.registry.blocks.FrequencerBlockEntity;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    @Inject(at = @At("TAIL"), method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V")
    private void simpleradio$playSeededSound1_audioGathering(Player except, Entity entity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
        RadioManager.getInstance().onSoundPlayed(except, entity.getX(), entity.getY(), entity.getZ(), sound, source);
    }

    @Inject(at = @At("TAIL"), method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V")
    private void simpleradio$playSeededSound2_audioGathering(Player except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed, CallbackInfo ci) {
        RadioManager.getInstance().onSoundPlayed(except, x, y, z, sound, source);
    }
}