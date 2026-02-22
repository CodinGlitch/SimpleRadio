package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.*;
import com.codinglitch.simpleradio.core.Frequencies;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioComponents;
import com.codinglitch.simpleradio.core.registry.SimpleRadioFrequencing;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.routers.*;
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

    private void activate(Level level, ItemStack stack, String frequencyName, String modulation, Entity entity, UUID owner) {
        Frequencies frequencies = SimpleRadioApi.getInstance(level.isClientSide).frequencies();

        Listener listener = startListening(entity, owner);
        Speaker speaker = startSpeaking(entity, owner);
        Receiver receiver = startReceiving(entity, frequencyName, frequencies.modulationOf(modulation), owner);
        Transmitter transmitter = startTransmitting(entity, frequencyName, frequencies.modulationOf(modulation), owner);

        if (speaker.getOwner().level() != level) {
            CommonSimpleRadio.info(level);
        }

        listener.setOwner(entity);
        speaker.setOwner(entity);
        receiver.setOwner(entity);
        transmitter.setOwner(entity);

        listener.tryAddRouter(transmitter);
        receiver.tryAddRouter(speaker);

        this.setupRouters(listener, speaker, receiver, transmitter);

        transmitter.setRoutingCriteria((source, router) -> {
            if (entity instanceof Player player) {
                ItemStack using = player.getUseItem();
                if (!(using.getItem() instanceof TransceiverItem)) return false;

                if (!using.has(SimpleRadioComponents.FREQUENCY) || !using.has(SimpleRadioComponents.MODULATION)) return false;
                if (!using.get(SimpleRadioComponents.FREQUENCY).equals(frequencyName) || !using.get(SimpleRadioComponents.MODULATION).equals(modulation)) return false;
            }

            Frequency frequency = getFrequency(stack);
            if (frequency == null) return false;

            return true;
        });
    }
    private void inactivate(Level level, String frequencyName, String modulation, UUID owner) {
        Frequencies frequencies = SimpleRadioApi.getInstance(level.isClientSide).frequencies();

        stopListening(owner, level.isClientSide);
        stopSpeaking(owner, level.isClientSide);
        stopReceiving(frequencyName, frequencies.modulationOf(modulation), owner);
        stopTransmitting(frequencyName, frequencies.modulationOf(modulation), owner);
    }

    public int getCooldown() {
        return 20;
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        super.verifyComponentsAfterLoad(stack);

        if (stack.has(SimpleRadioComponents.ACTIVATED))
            stack.remove(SimpleRadioComponents.ACTIVATED);
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        super.onDestroyed(itemEntity);

        ItemStack stack = itemEntity.getItem();
        if (stack.has(SimpleRadioComponents.FREQUENCY) && stack.has(SimpleRadioComponents.MODULATION) && stack.has(SimpleRadioComponents.MODULATION)) {
            inactivate(itemEntity.level(),
                    stack.get(SimpleRadioComponents.FREQUENCY),
                    stack.get(SimpleRadioComponents.MODULATION),
                    UUID.fromString(stack.get(SimpleRadioComponents.REFERENCE))
            );
        }
    }

    public void entityTick(ItemStack stack, Entity entity) {
        if (entity.isRemoved()) return;

        Level level = entity.level();

        String frequency = stack.get(SimpleRadioComponents.FREQUENCY);
        String modulation = stack.get(SimpleRadioComponents.MODULATION);
        tick(stack, level);
        if (frequency == null || modulation == null) return;

        Frequencies frequencies = SimpleRadioApi.getInstance(level.isClientSide).frequencies();
        if (!frequencies.check(frequency)) {
            CommonSimpleRadio.info("Invalid frequency {}, replacing with default", frequency);
            frequency = this.getDefaultFrequency();
            stack.set(SimpleRadioComponents.FREQUENCY, frequency);
        }

        // Mode-switch deactivation (i.e. item is dropped)
        Router activeRouter = null;
        if (stack.has(SimpleRadioComponents.REFERENCE)) {
            activeRouter = SimpleRadioApi.getRouterSided(
                    UUID.fromString(stack.get(SimpleRadioComponents.REFERENCE)), level.isClientSide
            );
        }

        if (activeRouter != null) {
            if (activeRouter.getOwner() == null) { // Invalid router, not ours
                activeRouter = null;
            } else if (!activeRouter.getOwner().getUUID().equals(entity.getUUID())) { // Found router does not match ours, discard
                activeRouter = null;
            } else if (stack.has(SimpleRadioComponents.REFERENCE)) { // Check for a duplicate UUID from a different ItemStack
                Iterable<ItemStack> items = List.of();
                if (entity instanceof Player player) {
                    items = player.getInventory().items;
                } else if (entity instanceof LivingEntity livingEntity) {
                    items = livingEntity.getAllSlots();
                }

                for (ItemStack slotStack : items) {
                    if (slotStack.isEmpty()) continue;

                    if (!slotStack.has(SimpleRadioComponents.REFERENCE)) continue;
                    if (!slotStack.get(SimpleRadioComponents.REFERENCE).equals(stack.get(SimpleRadioComponents.REFERENCE))) continue;

                    if (!slotStack.equals(stack)) {
                        stack.remove(SimpleRadioComponents.REFERENCE);
                        break;
                    }
                }

                if (!stack.has(SimpleRadioComponents.REFERENCE)) activeRouter = null;
            }
        }

        // Transceiver activation
        UUID activationUUID = null;
        if (entity.level().isClientSide) {

            if (stack.has(SimpleRadioComponents.REFERENCE) && activeRouter == null) {
                activationUUID = UUID.fromString(stack.get(SimpleRadioComponents.REFERENCE));
            }

        } else {
            if (activeRouter != null) return;

            if (!stack.has(SimpleRadioComponents.REFERENCE)) {
                activationUUID = UUID.randomUUID();
                stack.set(SimpleRadioComponents.REFERENCE, activationUUID.toString());
            } else {
                activationUUID = UUID.fromString(stack.get(SimpleRadioComponents.REFERENCE));
            }
        }

        if (activationUUID == null) return;

        CommonSimpleRadio.debug("Activated transceiver using UUID {}!", activationUUID);

        frequency = stack.get(SimpleRadioComponents.FREQUENCY);
        modulation = stack.get(SimpleRadioComponents.MODULATION);
        activate(level, stack, frequency, modulation, entity, activationUUID);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltip) {
        appendTooltip(stack, components);
        super.appendHoverText(stack, context, components, tooltip);
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

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
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

            player.getCooldowns().addCooldown(this, this.getCooldown());
        }

        super.releaseUsing(stack, level, user, remainingUseTicks);
    }
}
