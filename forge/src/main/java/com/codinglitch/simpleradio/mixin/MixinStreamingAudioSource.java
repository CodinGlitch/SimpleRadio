package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.compat.create.CreateCompat;
import com.simibubi.create.content.contraptions.Contraption;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.sound.source.StreamingAudioSource;
import gg.moonflower.etched.api.util.DownloadProgressListener;
import gg.moonflower.etched.api.util.StreamingInputStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.IntFunction;

@Mixin(StreamingAudioSource.class)
public abstract class MixinStreamingAudioSource {

    @Shadow
    @Final
    private URL[] urls;
    @Shadow
    @Final
    private boolean temporary;
    @Shadow
    @Final
    private AudioSource.AudioFileType type;
    @Shadow
    @Final
    private CompletableFuture<?> downloadFuture;

    @Unique
    private IntFunction<CompletableFuture<InputStream>> source;

    @Inject(method = "openStream", at = @At("HEAD"), remap = false, require = 0, cancellable = true)
    private void simpleradio$openStream(CallbackInfoReturnable<CompletableFuture<InputStream>> cir) {
        if (!CompatCore.ETCHED.enabled) return;
        if (!SimpleRadioLibrary.SERVER_CONFIG.compatibilities.etched.streamPatch) return;

        if (this.source == null) {
            this.source = (i) -> CompletableFuture.supplyAsync(() -> AudioSource.downloadTo(this.urls[i], this.temporary, null, this.type), HttpUtil.DOWNLOAD_EXECUTOR).thenApplyAsync((stream) -> {
                try {
                    return stream.get();
                } catch (Exception e) {
                    throw new CompletionException("Failed to open channel", e);
                }
            }, Util.ioPool());
        }

        cir.setReturnValue(this.downloadFuture.thenApplyAsync((__) -> {
            try {
                return new StreamingInputStream(this.urls, this.source);
            } catch (Exception e) {
                throw new CompletionException("Failed to open stream", e);
            }
        }, Util.ioPool()));
    }
}
