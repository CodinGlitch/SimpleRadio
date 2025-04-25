package com.codinglitch.simpleradio.mixin;

import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.core.registry.blocks.FrequencerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    @Inject(at = @At("TAIL"), method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V")
    private void simpleradio$logChatMessage_frequencerSetting(PlayerChatMessage message, ServerPlayer player, ChatType.Bound $$2, CallbackInfo ci) {
        String content = message.signedContent().plain();
        if (content.startsWith("frequency")) {
            Frequency frequency = Frequency.tryParse(content.substring(10));
            ServerLevel level = (ServerLevel) player.level;
            BlockPos playerPosition = player.blockPosition();

            for (int x = -2; x < 2; x++) {
                for (int y = -2; y < 2; y++) {
                    for (int z = -2; z < 2; z++) {
                        BlockPos pos = playerPosition.offset(x, y, z);
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity instanceof FrequencerBlockEntity frequencerBlockEntity) {
                            frequencerBlockEntity.setFrequency(frequency);
                        }
                    }
                }
            }
        }
    }
}