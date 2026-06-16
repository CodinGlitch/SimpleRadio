package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioFrequencing;
import com.codinglitch.simpleradio.radio.*;
import com.codinglitch.simpleradio.routers.Listener;
import com.codinglitch.simpleradio.routers.Receiver;
import com.codinglitch.simpleradio.routers.Speaker;
import com.codinglitch.simpleradio.routers.Transmitter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.EntityPositionSource;

import java.util.Random;

public class WalkieTalkieItem extends TransceiverItem implements WorldTicking {
    public WalkieTalkieItem(Properties settings) {
        super(settings);
    }

    private Random RANDOM = new Random();

    @Override
    protected void setupRouters(Listener listener, Speaker speaker, Receiver receiver, Transmitter transmitter) {
        speaker.setRange(SimpleRadioLibrary.SERVER_CONFIG.walkie_talkie.speakingRange);
        listener.setRange(SimpleRadioLibrary.SERVER_CONFIG.walkie_talkie.listeningRange);
        speaker.setCategory(CommonRadioPlugin.WALKIES_CATEGORY);

        transmitter.frequencingType(SimpleRadioFrequencing.WALKIE_TALKIE);
        receiver.frequencingType(SimpleRadioFrequencing.WALKIE_TALKIE);

        listener.setLink(this.getClass());
        speaker.setLink(this.getClass());
        receiver.setLink(this.getClass());
        transmitter.setLink(this.getClass());
    }

    @Override
    public void begin(ItemStack stack, Level level) {
        // Get the receiver and deactivate it (half-duplex)
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("reference") && tag.contains("frequency") && tag.contains("modulation")) {
            Frequency frequency = SimpleRadioApi.getInstance(level.isClientSide).frequencies().get(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")));
            Receiver receiver = frequency.getReceiver(tag.getUUID("reference"));
            if (receiver != null) receiver.setActive(false);
        }

        super.begin(stack, level);
    }

    @Override
    public void end(ItemStack stack, Level level) {
        // Get the receiver and reactivate it (half-duplex)
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("reference") && tag.contains("frequency") && tag.contains("modulation")) {
            Frequency frequency = SimpleRadioApi.getInstance(level.isClientSide).frequencies().get(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")));
            Receiver receiver = frequency.getReceiver(tag.getUUID("reference"));
            if (receiver != null) receiver.setActive(true);
        }

        super.end(stack, level);
    }

    @Override
    public String getDefaultFrequency() {
        StringBuilder frequency = new StringBuilder();

        for (int i = 0; i < SimpleRadioLibrary.SERVER_CONFIG.frequency.wholePlaces; i++) {
            frequency.append(RANDOM.nextInt(0, 9));
        }
        frequency.append(".");
        for (int i = 0; i < SimpleRadioLibrary.SERVER_CONFIG.frequency.decimalPlaces; i++) {
            frequency.append(RANDOM.nextInt(0, 9));
        }

        return frequency.toString();
    }

    @Override
    public void worldTick(ItemEntity item, Level level) {
        ItemStack myStack = item.getItem();
        this.tick(myStack, level);

        if (item.tickCount > 60 && item.tickCount % 10 == 0) {
            CompoundTag myTag = myStack.getOrCreateTag();

            for (Entity entity : level.getEntities(item, item.getBoundingBox().inflate(1.0d))) {
                if (entity instanceof ItemEntity otherItem) {
                    if (!(otherItem.getItem().getItem() instanceof WalkieTalkieItem)) continue;

                    ItemStack theirStack = otherItem.getItem();
                    CompoundTag theirTag = theirStack.getOrCreateTag();
                    if (!theirTag.contains("frequency")) continue;
                    if (!theirTag.contains("modulation")) continue;
                    if (theirTag.getString("frequency").equals(myTag.getString("frequency")) &&
                            theirTag.getString("modulation").equals(myTag.getString("modulation"))) continue;

                    myTag.putString("frequency", theirTag.getString("frequency"));
                    myTag.putString("modulation", theirTag.getString("modulation"));

                    level.playSound(null, item, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.MASTER, 1, 1);

                    for (int i = 0; i < 3; i++) {
                        level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                                item.getRandomX(0.5D), 0.25D + item.getRandomY(), item.getRandomZ(0.5D),
                                RANDOM.nextDouble(-0.2, 0.2), RANDOM.nextDouble(-0.2, 0.2), RANDOM.nextDouble(-0.2, 0.2)
                        );
                    }

                    level.addParticle(new VibrationParticleOption(new EntityPositionSource(item, 0.25f), 5), otherItem.getX(), otherItem.getY() + 0.25f, otherItem.getZ(), 0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public int getCooldown() {
        return SimpleRadioLibrary.SERVER_CONFIG.walkie_talkie.cooldown;
    }
}
