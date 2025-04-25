package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class MixinJukeboxBlockEntity extends BlockEntity implements Clearable {
    @Shadow private ItemStack record;

    public MixinJukeboxBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
        super($$0, $$1, $$2);
    }

    @Inject(method = "playRecord()V", at = @At(value = "TAIL"))
    private void simpleradio$startPlaying_audioGathering(CallbackInfo ci) {
        Item item = this.record.getItem();

        if (item instanceof RecordItem recordItem && level instanceof ServerLevel serverLevel) {
            RadioManager.getInstance().onSoundPlayed(
                    serverLevel,
                    Vec3.atCenterOf(getBlockPos()),
                    recordItem.getSound(),
                    1, 1, this.getBlockPos().asLong()
            );
        }
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V"
            ), method = "playRecordTick"
    )
    private static void simpleradio$stopPlaying_audioGathering(Level level, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            RadioManager.getInstance().onSoundPlayed(
                    serverLevel,
                    Vec3.atCenterOf(blockEntity.getBlockPos()),
                    SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE,
                    0, 1, blockEntity.getBlockPos().asLong()
            );
        }
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V"
            ), method = "playRecordTick"
    )
    private static void simpleradio$tick_audioGathering(Level level, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity, CallbackInfo ci) {
        Item item = blockEntity.getRecord().getItem();

        if (item instanceof RecordItem recordItem && level instanceof ServerLevel serverLevel) {
            float offset = (blockEntity.tickCount - blockEntity.recordStartedTick) / 20f;

            RadioManager.getInstance().onSoundPlayed(
                    serverLevel,
                    Vec3.atCenterOf(blockEntity.getBlockPos()),
                    recordItem.getSound(),
                    1, 1,  offset, blockEntity.getBlockPos().asLong()
            );
        }
    }
}