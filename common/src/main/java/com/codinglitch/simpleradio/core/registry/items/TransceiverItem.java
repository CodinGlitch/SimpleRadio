package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.core.central.Transceiving;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundRadioPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.core.central.Frequency;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TransceiverItem extends Item implements Transceiving {
    public TransceiverItem(Properties settings) {
        super(settings);
    }

    private void transmit(ServerPlayer player, boolean started) {
        Services.NETWORKING.sendToPlayer(player, new ClientboundRadioPacket(started, player.getUUID()));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);
        tick(stack, level, entity);

        if (entity instanceof Player player && !level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();

            String frequency = tag.getString("frequency");
            String modulation = tag.getString("modulation");

            UUID playerUUID = player.getUUID();
            if (tag.contains("user")) {
                UUID currentUUID = tag.getUUID("user");
                if (currentUUID.equals(playerUUID)) return;

                stopListening(frequency, Frequency.modulationOf(modulation), currentUUID);
            }

            listen(frequency, Frequency.modulationOf(modulation), playerUUID);
            tag.putUUID("user", playerUUID);
        }

    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltip) {
        appendTooltip(stack, components);
        super.appendHoverText(stack, level, components, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                player, player.blockPosition(),
                SimpleRadioSounds.RADIO_OPEN,
                SoundSource.PLAYERS,
                1f,1f
        );
        player.startUsingItem(hand);

        // Send started using packet
        if (!level.isClientSide) {
            transmit((ServerPlayer) player, true);
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
                    SimpleRadioSounds.RADIO_CLOSE,
                    SoundSource.PLAYERS,
                    1f,1f
            );

            // Send stopped using packet
            if (!level.isClientSide) {
                transmit((ServerPlayer) player, false);
            }

            player.getCooldowns().addCooldown(this, 20);
        }

        super.releaseUsing(stack, level, user, remainingUseTicks);
    }
}
