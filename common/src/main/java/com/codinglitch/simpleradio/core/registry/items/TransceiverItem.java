package com.codinglitch.simpleradio.core.registry.items;

import codinglit.ch.simpleradio.SimpleRadio;
import codinglit.ch.simpleradio.SimpleRadioNetworking;
import com.codinglitch.simpleradio.CommonSimpleRadio;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerGroupManager;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.Settings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.World;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class TransceiverItem extends Item {
    public TransceiverItem(Settings settings) {
        super(settings);
    }

    private void transmit(ServerPlayer player, ResourceLocation packetID) {
        Server server = Voicechat.SERVER.getServer();
        if (server != null) {
            ServerGroupManager groupManager = server.getGroupManager();

            Collection<ServerPlayer> players = ServerWorldUtils.getPlayersInRange((ServerLevel) player.level(), player.blockPosition(), CommonSimpleRadio.SERVER_CONFIG.transceiver.maxTransceiverDistance, receiver -> {
                Group groupSender = groupManager.getPlayerGroup(player);
                Group groupReceiver = groupManager.getPlayerGroup(receiver);
                return groupSender != null && groupSender.equals(groupReceiver) && receiver.getUUID() != player.getUUID();
            });

            PacketByteBuf buffer = PacketByteBufs.create().writeUuid(player.getUUID());
            for (ServerPlayer receiver : players) {
                ServerPlayNetworking.send(receiver, packetID, buffer);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                player, player.blockPosition(),
                CommonSimpleRadio.RADIO_OPEN,
                SoundSource.PLAYERS,
                1f,1f
        );
        player.startUsingItem(hand);

        stack.getOrCreateTag().putFloat("frequency", 123.54f);

        // Send started using packet
        if (!level.isClientSide) {
            transmit((ServerPlayer) player, SimpleRadioNetworking.STARTED_USING_RADIO_S2C);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player player) {
            level.playSound(
                    player, user.blockPosition(),
                    CommonSimpleRadio.RADIO_CLOSE,
                    SoundSource.PLAYERS,
                    1f,1f
            );

            // Send stopped using packet
            if (!level.isClientSide) {
                transmit((ServerPlayer) player, SimpleRadioNetworking.STOPPED_USING_RADIO_S2C);
            }

            player.getCooldowns().addCooldown(this, 20);
        }

        super.releaseUsing(stack, level, user, remainingUseTicks);
    }
}
