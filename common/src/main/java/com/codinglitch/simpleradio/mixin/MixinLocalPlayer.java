package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import com.codinglitch.simpleradio.core.registry.items.WalkieTalkieItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {

    public MixinLocalPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    @Shadow public abstract boolean isUsingItem();

    @Shadow @Nullable private InteractionHand usingItemHand;

    @Unique
    private boolean simpleradio$willSlow_transceiverSlowing(LocalPlayer player, Operation<Boolean> original) {
        if (this.isUsingItem()) {
            ItemStack stack = player.getItemInHand(player.getUsedItemHand());
            if (stack.getItem().getClass() == TransceiverItem.class) {
                return SimpleRadioLibrary.CLIENT_CONFIG.transceiver.transceiverSlow;
            } else if (stack.getItem().getClass() == WalkieTalkieItem.class) {
                return SimpleRadioLibrary.CLIENT_CONFIG.walkie_talkie.walkieTalkieSlow;
            }
        }
        return original.call(player);
    }

    @WrapOperation(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")
    )
    private boolean simpleradio$aiStep_transceiverSlowing(LocalPlayer instance, Operation<Boolean> original) {
        return simpleradio$willSlow_transceiverSlowing(instance, original);
    }
    @WrapOperation(
            method = "canStartSprinting",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")
    )
    private boolean simpleradio$canStartSprinting_transceiverSlowing(LocalPlayer instance, Operation<Boolean> original) {
        return simpleradio$willSlow_transceiverSlowing(instance, original);
    }
}