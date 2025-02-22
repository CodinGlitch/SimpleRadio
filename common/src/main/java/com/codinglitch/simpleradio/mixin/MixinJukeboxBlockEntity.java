package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class MixinJukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem {

    @Shadow private long recordStartedTick;

    @Shadow private long tickCount;

    public MixinJukeboxBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
        super($$0, $$1, $$2);
    }

    @Inject(method = "startPlaying()V", at = @At(value = "TAIL"))
    private void simpleradio$startPlaying_audioGathering(CallbackInfo ci) {
        Item item = this.getFirstItem().getItem();

        if (item instanceof RecordItem recordItem && level instanceof ServerLevel serverLevel) {
            RadioManager.getInstance().onSoundPlayed(
                    serverLevel,
                    getBlockPos().getCenter(),
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(recordItem.getSound()),
                    1, 1, 12
            );
        }
    }

    @Inject(method = "stopPlaying()V", at = @At(value = "TAIL"))
    private void simpleradio$stopPlaying_audioGathering(CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            RadioManager.getInstance().onSoundPlayed(
                    serverLevel,
                    getBlockPos().getCenter(),
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY),
                    0, 1, 12
            );
        }
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/world/level/block/entity/JukeboxBlockEntity;spawnMusicParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
            ), method = "tick"
    )
    private void simpleradio$tick_audioGathering(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        Item item = this.getFirstItem().getItem();

        if (item instanceof RecordItem recordItem && level instanceof ServerLevel serverLevel) {
            float offset = (tickCount - recordStartedTick) / 20f;

            RadioManager.getInstance().onSoundPlayed(
                    serverLevel,
                    getBlockPos().getCenter(),
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(recordItem.getSound()),
                    1, 1,  offset, 12
            );
        }
    }
}