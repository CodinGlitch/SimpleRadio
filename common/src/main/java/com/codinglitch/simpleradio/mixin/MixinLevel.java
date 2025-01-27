package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.radio.RadioManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class MixinLevel {

    @Shadow @Final public boolean isClientSide;

    @Inject(at = @At("TAIL"), method = "close")
    private void simpleradio$close(CallbackInfo info) {
        if (this.isClientSide) {
            ClientRadioManager.close();
        } else {
            RadioManager.close();
        }
    }
}