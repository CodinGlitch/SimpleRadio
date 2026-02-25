package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioFrequencing;
import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.routers.Listener;
import com.codinglitch.simpleradio.routers.Receiver;
import com.codinglitch.simpleradio.routers.Speaker;
import com.codinglitch.simpleradio.routers.Transmitter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.EntityPositionSource;

import java.util.Random;

import static com.codinglitch.simpleradio.core.SimpleRadioComponents.FREQUENCY;
import static com.codinglitch.simpleradio.core.SimpleRadioComponents.MODULATION;

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

        // --- Half-duplex implementation

        receiver.setAcceptingCriteria(((source) -> {
            Entity entity = receiver.getOwner();
            Frequency frequency = receiver.getFrequency();
            if (frequency == null) return false;

            if (entity instanceof Player player) {
                ItemStack using = player.getUseItem();

                if (!using.has(FREQUENCY) || !using.has(MODULATION)) return true;

                if (!(using.getItem() instanceof TransceiverItem)) return true;
                if (!frequency.getFrequency().equals(using.get(FREQUENCY))) return true;
                return !frequency.getModulation().shorthand.equals(using.get(MODULATION));
            }

            return true;
        }));
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
            for (Entity entity : level.getEntities(item, item.getBoundingBox().inflate(1.0d))) {
                if (entity instanceof ItemEntity otherItem) {
                    if (!(otherItem.getItem().getItem() instanceof WalkieTalkieItem)) continue;

                    ItemStack theirStack = otherItem.getItem();
                    if (!theirStack.has(FREQUENCY)) continue;
                    if (!theirStack.has(MODULATION)) continue;
                    if (theirStack.get(FREQUENCY).equals(myStack.get(FREQUENCY)) &&
                            theirStack.get(MODULATION).equals(myStack.get(MODULATION))) continue;

                    myStack.set(FREQUENCY,  theirStack.get(FREQUENCY));
                    myStack.set(MODULATION, theirStack.get(MODULATION));

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
        return 60;
    }
}
