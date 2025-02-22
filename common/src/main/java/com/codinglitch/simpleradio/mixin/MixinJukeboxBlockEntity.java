package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class MixinJukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem {

    public MixinJukeboxBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
        super($$0, $$1, $$2);
    }

    @Inject(method = "startPlaying()V", at = @At(value = "TAIL"))
    private void simpleradio$startPlaying_audioGathering(CallbackInfo ci) {
        Item item = this.getFirstItem().getItem();

        if (item instanceof RecordItem recordItem && level instanceof ServerLevel serverLevel) {
            RadioManager.getInstance().onSoundPlayed(
                    null, serverLevel,
                    getBlockPos().getCenter(),
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(recordItem.getSound()),
                    SoundSource.RECORDS,
                    1, 1
            );
        }
    }
}