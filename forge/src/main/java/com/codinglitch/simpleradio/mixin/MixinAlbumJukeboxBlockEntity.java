package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioManager;
import gg.moonflower.etched.api.sound.source.AudioSource;
import gg.moonflower.etched.api.sound.source.StreamingAudioSource;
import gg.moonflower.etched.api.util.StreamingInputStream;
import gg.moonflower.etched.common.blockentity.AlbumJukeboxBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.units.qual.A;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.IntFunction;

@Mixin(AlbumJukeboxBlockEntity.class)
public abstract class MixinAlbumJukeboxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {

    protected MixinAlbumJukeboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public abstract int getPlayingIndex();

    @Shadow
    protected abstract NonNullList<ItemStack> getItems();

    @Unique
    private void simpleradio$update() {
        if (!CompatCore.ETCHED.enabled) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (this.getPlayingIndex() >= 0) {
            List<String> parts = new ArrayList<>();
            parts.add(String.valueOf(this.getPlayingIndex()));
            for (ItemStack item : this.getItems()) {
                if (item.isEmpty()) continue;
                parts.add(CompatCore.getSound(item));
            }

            RadioManager.getInstance().sendSound(
                    WorldlyPosition.of(getBlockPos().getCenter().toVector3f(), serverLevel),
                    String.join("|", parts),
                    1, 1, this.getBlockPos().asLong()
            );
        } else {
            RadioManager.getInstance().stopRecord(serverLevel, this.getBlockPos().asLong());
        }
    }

    @Inject(method = "updateState", at = @At(value = "INVOKE", target = "Lgg/moonflower/etched/common/blockentity/AlbumJukeboxBlockEntity;setChanged()V"), remap = false, require = 0)
    private void simpleradio$updateState(CallbackInfo ci) {
        simpleradio$update();
    }

    @Inject(method = "nextPlayingIndex", at = @At("TAIL"), remap = false, require = 0)
    private void simpleradio$nextPlayingIndex(boolean reverse, CallbackInfo ci) {
        simpleradio$update();
    }

    @Inject(method = "setPlayingIndex", at = @At("TAIL"), remap = false, require = 0)
    private void simpleradio$setPlayingIndex(int playingIndex, int track, CallbackInfoReturnable<Boolean> cir) {
        simpleradio$update();
    }
}
