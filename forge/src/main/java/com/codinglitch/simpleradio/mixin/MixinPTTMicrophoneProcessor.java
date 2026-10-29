package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.maxhenkel.voicechat.voice.client.PTTMicrophoneProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PTTMicrophoneProcessor.class)
public abstract class MixinPTTMicrophoneProcessor {

    @WrapOperation(
            method = "processInternal",
            at = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/voice/client/PTTMicrophoneProcessor;isPttButtonDown()Z"),
            remap = false
    )
    private boolean simpleradio$processInternal_handheldPTT(PTTMicrophoneProcessor instance, Operation<Boolean> original) {
        return ClientRadioManager.shouldEnablePTT() || original.call(instance);
    }
}
