package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.CompatCore;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity {
    @Inject(method = "setRemoved", at = @At(value = "TAIL"))
    private void simpleradio$setRemoved(CallbackInfo ci) {
        CompatCore.removeBlockEntity((BlockEntity) (Object) this);
    }
}