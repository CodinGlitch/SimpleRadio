package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {

    @Shadow public abstract LevelData getLevelData();

    @Inject(at = @At("TAIL"), method = "tick")
    private void simpleradio$tick(CallbackInfo info) {
        ClientRadioManager.tick(this.getLevelData().getGameTime());
    }

    @Inject(at = @At("TAIL"), method = "disconnect")
    private void simpleradio$disconnect(CallbackInfo info) {
        ClientRadioManager.close();
    }
}