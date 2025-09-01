package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.radio.RadioManager;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.plugins.impl.audiochannel.LocationalAudioChannelImpl;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocationalAudioChannelImpl.class)
public abstract class MixinLocationalAudioChannelImpl {

    @Shadow protected ServerLevel level;

    @Inject(method = "broadcast", at = @At("HEAD"), remap = false)
    private void simpleradio$broadcast_hookPacket(LocationSoundPacket packet, CallbackInfo ci) {
        RadioManager.getInstance().onLocationalPacket((Level) this.level.getServerLevel(), (LocationalAudioChannel) this, packet.getData());
    }
}