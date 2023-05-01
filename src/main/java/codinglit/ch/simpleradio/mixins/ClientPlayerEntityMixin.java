package codinglit.ch.simpleradio.mixins;

import codinglit.ch.simpleradio.registry.RadioItem;
import codinglit.ch.simpleradio.SimpleRadio;
import codinglit.ch.simpleradio.client.SimpleRadioClient;
import com.mojang.authlib.GameProfile;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin({ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Redirect(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/Input;movementSideways:F", opcode = Opcodes.PUTFIELD))
    private void simpleradio$movementSideways(Input input, float value) {
        if (this.getActiveItem().getItem() instanceof RadioItem)
            input.movementSideways = value * 5; //this may or may not be a bad idea
        else
            input.movementSideways = value;
    }

    @Redirect(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/Input;movementForward:F", opcode = Opcodes.PUTFIELD))
    private void simpleradio$movementForward(Input input, float value) {
        if (this.getActiveItem().getItem() instanceof RadioItem)
            input.movementForward = value * 5;
        else
            input.movementForward = value;
    }
}
