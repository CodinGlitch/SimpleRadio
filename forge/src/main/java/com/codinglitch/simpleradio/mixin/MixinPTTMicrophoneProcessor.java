package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.maxhenkel.voicechat.voice.client.PTTMicrophoneProcessor;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.sound.source.StreamingAudioSource;
import gg.moonflower.etched.api.util.StreamingInputStream;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.HttpUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.IntFunction;

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
