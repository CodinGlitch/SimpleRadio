package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class MixinExplosion {
    @Shadow @Final private Level level;

    @Shadow @Final private double x;

    @Shadow @Final private double y;

    @Shadow @Final private double z;

    @Inject(at = @At("HEAD"), method = "finalizeExplosion")
    private void simpleradio$finalizeExplosion(CallbackInfo info) {
        if (this.level instanceof ServerLevel serverLevel) {
            RadioManager.getInstance().sendSound(new WorldlyPosition((float) this.x, (float) this.y, (float) this.z, serverLevel), SoundEvents.GENERIC_EXPLODE.value(), 4, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, 1);
        }
    }
}