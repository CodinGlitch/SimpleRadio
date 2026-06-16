package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.ServerSimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.*;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioFrequencing;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.radio.*;
import com.codinglitch.simpleradio.routers.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class TransceiverItem extends Item implements Listening, Speaking, Receiving, Transmitting, WorldTicking {
    public TransceiverItem(Properties settings) {
        super(settings);
    }

    protected void setupRouters(Listener listener, Speaker speaker, Receiver receiver, Transmitter transmitter) {
        speaker.setRange(SimpleRadioLibrary.SERVER_CONFIG.transceiver.speakingRange);
        listener.setRange(SimpleRadioLibrary.SERVER_CONFIG.transceiver.listeningRange);
        speaker.setCategory(CommonRadioPlugin.TRANSCEIVERS_CATEGORY);

        transmitter.frequencingType(SimpleRadioFrequencing.TRANSCEIVER);
        receiver.frequencingType(SimpleRadioFrequencing.TRANSCEIVER);

        listener.setLink(this.getClass());
        speaker.setLink(this.getClass());
        receiver.setLink(this.getClass());
        transmitter.setLink(this.getClass());
    }

    public void activate(Level level, ItemStack stack, String frequencyName, Frequency.Modulation modulation, Entity entity, UUID owner) {
        CommonSimpleRadio.info("Activating transceiver with reference {}", owner);
        Frequencies frequencies = SimpleRadioApi.getInstance(level.isClientSide).frequencies();

        Listener listener = startListening(entity, owner);
        Speaker speaker = startSpeaking(entity, owner);
        Receiver receiver = startReceiving(entity, frequencyName, modulation, owner);
        Transmitter transmitter = startTransmitting(entity, frequencyName, modulation, owner);

        listener.setOwner(entity);
        speaker.setOwner(entity);
        receiver.setOwner(entity);
        transmitter.setOwner(entity);

        listener.tryAddRouter(transmitter);
        receiver.tryAddRouter(speaker);

        this.setupRouters(listener, speaker, receiver, transmitter);

        // Set transmitter activation state only if a player isn't holding it
        transmitter.setActive(!(entity instanceof Player));
    }
    public void inactivate(Level level, String frequencyName, Frequency.Modulation modulation, UUID owner) {
        stopListening(owner, level.isClientSide);
        stopSpeaking(owner, level.isClientSide);
        stopReceiving(frequencyName, modulation, owner, level.isClientSide);
        stopTransmitting(frequencyName, modulation, owner, level.isClientSide);
    }

    public void begin(ItemStack stack, Entity entity) {
        Level level = entity.level();

        // Play the sound
        level.playSound(
                entity, entity.blockPosition(),
                SimpleRadioSounds.RADIO_OPEN,
                SoundSource.PLAYERS,
                1f,1f
        );

        // Get the transmitter and activate it
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("reference") && tag.contains("frequency") && tag.contains("modulation")) {
            Frequency frequency = SimpleRadioApi.getInstance(level.isClientSide).frequencies().get(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")));
            Transmitter transmitter = frequency.getTransmitter(tag.getUUID("reference"));
            if (transmitter != null) transmitter.setActive(true);
        }
    }
    public void end(ItemStack stack, Entity entity) {
        Level level = entity.level();

        // Play the sound
        level.playSound(
                entity, entity.blockPosition(),
                SimpleRadioSounds.RADIO_CLOSE,
                SoundSource.PLAYERS,
                1f,1f
        );

        // Add cooldowns
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, this.getCooldown());
        }

        // Get the transmitter and deactivate it
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("reference") && tag.contains("frequency") && tag.contains("modulation")) {
            Frequency frequency = SimpleRadioApi.getInstance(level.isClientSide).frequencies().get(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")));
            Transmitter transmitter = frequency.getTransmitter(tag.getUUID("reference"));
            if (transmitter != null) transmitter.setActive(false);
        }
    }

    public int getCooldown() {
        return SimpleRadioLibrary.SERVER_CONFIG.transceiver.cooldown;
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag) {
        super.verifyTagAfterLoad(tag);

        if (tag.contains("activated"))
            tag.remove("activated");
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        super.onDestroyed(itemEntity);
        CompoundTag tag = itemEntity.getItem().getOrCreateTag();
        if (tag.contains("frequency") && tag.contains("modulation") && tag.contains("reference")) {
            inactivate(itemEntity.level(), tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")), tag.getUUID("reference"));
        }
    }

    public void entityTick(ItemStack stack, Entity entity) {
        if (entity.isRemoved()) return;

        Level level = entity.level();
        CompoundTag tag = stack.getOrCreateTag();

        String frequency = tag.getString("frequency");
        String modulation = tag.getString("modulation");
        tick(stack, level);
        if (frequency.isEmpty() || modulation.isEmpty()) return;

        Frequencies frequencies = SimpleRadioApi.getInstance(level.isClientSide).frequencies();
        if (!frequencies.check(frequency)) {
            CommonSimpleRadio.info("Invalid frequency {}, replacing with default", frequency);
            frequency = this.getDefaultFrequency();
            tag.putString("frequency", frequency);
        }

        // Mode-switch deactivation (i.e. item is dropped)
        Router activeRouter = null;
        if (tag.contains("reference")) {
            activeRouter = SimpleRadioApi.getRouterSided(tag.getUUID("reference"), level.isClientSide);
        }

        if (activeRouter != null) {
            if (activeRouter.getOwner() == null) { // Invalid router, not ours
                activeRouter = null;
            } else if (!activeRouter.getOwner().getUUID().equals(entity.getUUID())) { // Found router does not match ours, discard
                activeRouter = null;
                //if (tag.contains("reference")) tag.remove("reference");
            } else if (tag.contains("reference")) { // Check for a duplicate UUID from a different ItemStack
                Iterable<ItemStack> items = List.of();
                if (entity instanceof Player player) {
                    items = player.getInventory().items;
                } else if (entity instanceof LivingEntity livingEntity) {
                    items = livingEntity.getAllSlots();
                }

                for (ItemStack slotStack : items) {
                    if (slotStack.isEmpty()) continue;
                    if (!slotStack.hasTag()) continue;

                    CompoundTag slotTag = slotStack.getTag();
                    if (slotTag == null) continue;
                    if (!slotTag.contains("reference")) continue;
                    if (!slotTag.getUUID("reference").equals(tag.getUUID("reference"))) continue;

                    if (!slotStack.equals(stack)) {
                        tag.remove("reference");
                        break;
                    }
                }

                if (!tag.contains("reference")) activeRouter = null;
            }
        }

        // Transceiver activation
        UUID activationUUID = null;
        if (entity.level().isClientSide) {

            if (tag.contains("reference") && activeRouter == null) {
                activationUUID = tag.getUUID("reference");
            }

        } else {
            if (activeRouter != null) return;

            if (!tag.contains("reference")) {
                activationUUID = UUID.randomUUID();
                tag.putUUID("reference", activationUUID);
            } else {
                activationUUID = tag.getUUID("reference");
            }
        }

        if (activationUUID == null) return;

        CommonSimpleRadio.debug("Activated transceiver using UUID {}!", activationUUID);

        frequency = tag.getString("frequency");
        modulation = tag.getString("modulation");
        activate(level, stack, frequency, Frequency.modulationOf(modulation), entity, activationUUID);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (selected) return;

        entityTick(stack, entity);
    }

    @Override
    public void worldTick(ItemEntity item, Level level) {
        entityTick(item.getItem(), item);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltip) {
        appendTooltip(stack, components);
        super.appendHoverText(stack, level, components, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);

        ItemStack stack = player.getItemInHand(hand);
        begin(stack, player);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        end(stack, user);

        super.releaseUsing(stack, level, user, remainingUseTicks);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }
}
