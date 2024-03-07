package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.Receiving;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundRadioPacket;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.core.central.Frequency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TransceiverItem extends Item implements Receiving {
    public TransceiverItem(Properties settings) {
        super(settings);
    }

    private void transmit(ServerPlayer player, boolean started) {
        Services.NETWORKING.sendToPlayer(player, new ClientboundRadioPacket(started, player.getUUID()));
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag) {
        super.verifyTagAfterLoad(tag);

        if (tag.contains("user"))
            tag.remove("user");
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        super.onDestroyed(itemEntity);
        CompoundTag tag = itemEntity.getItem().getOrCreateTag();
        if (tag.contains("frequency") && tag.contains("modulation") && tag.contains("user"))
            stopListening(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")), tag.getUUID("user"));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);

        CompoundTag tag = stack.getOrCreateTag();

        String frequency = tag.getString("frequency");
        String modulation = tag.getString("modulation");
        tick(stack, level, entity);
        if (frequency.isEmpty() || modulation.isEmpty()) return;

        if (!Frequency.validate(frequency)) {
            CommonSimpleRadio.info("Invalid frequency {}, replacing with default", frequency);
            frequency = Frequency.DEFAULT_FREQUENCY;
            tag.putString("frequency", Frequency.DEFAULT_FREQUENCY);
        }

        UUID uuid = entity.getUUID();
        if (tag.contains("user")) {
            UUID currentUUID = tag.getUUID("user");
            if (currentUUID.equals(uuid)) {
                if (validate(frequency, Frequency.modulationOf(modulation), currentUUID)) return;
            } else {
                if (!level.isClientSide)
                    stopListening(frequency, Frequency.modulationOf(modulation), currentUUID);
            }
        }

        frequency = tag.getString("frequency");
        modulation = tag.getString("modulation");
        if (!level.isClientSide)
            listen(frequency, Frequency.modulationOf(modulation), uuid);
        tag.putUUID("user", uuid);
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
