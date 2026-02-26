package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxSongPlayer.class)
public abstract class MixinJukeboxSongPlayer {

    @Shadow
    @Final
    private BlockPos blockPos;

    @Shadow
    public abstract long getTicksSinceSongStarted();

    @Inject(method = "play", at = @At(value = "TAIL"))
    private void simpleradio$startPlaying_audioGathering(LevelAccessor levelAccessor, Holder<JukeboxSong> songHolder, CallbackInfo ci) {
        BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
        if (!(blockEntity instanceof JukeboxBlockEntity jukebox)) return;

        if (levelAccessor instanceof ServerLevel serverLevel) {
            RadioManager.getInstance().sendRecord(
                    jukebox.getTheItem(),
                    WorldlyPosition.of(blockPos.getCenter().toVector3f(), serverLevel),
                    blockPos.asLong()
            );
        }
    }

    @Inject(method = "stop", at = @At(value = "TAIL"))
    private void simpleradio$stopPlaying_audioGathering(LevelAccessor levelAccessor, BlockState state, CallbackInfo ci) {
        BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
        if (!(blockEntity instanceof JukeboxBlockEntity jukebox)) return;

        if (levelAccessor instanceof ServerLevel serverLevel) {
            RadioManager.getInstance().stopRecord(serverLevel, blockPos.asLong());
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void simpleradio$tick_audioGathering(LevelAccessor levelAccessor, @Nullable BlockState state, CallbackInfo ci) {
        BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
        if (!(blockEntity instanceof JukeboxBlockEntity jukebox)) return;

        if (levelAccessor instanceof ServerLevel serverLevel) {
            float offset = getTicksSinceSongStarted();

            RadioManager.getInstance().updateRecord(
                    jukebox.getTheItem(),
                    WorldlyPosition.of(jukebox.getBlockPos().getCenter().toVector3f(), serverLevel),
                    offset,
                    jukebox.getBlockPos().asLong()
            );
        }
    }
}