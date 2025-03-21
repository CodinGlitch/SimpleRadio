package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.SimpleRadioFrequencing;
import com.codinglitch.simpleradio.radio.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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
    protected void setupRouters(RadioListener listener, RadioSpeaker speaker, RadioReceiver receiver, RadioTransmitter transmitter) {
        speaker.range = SimpleRadioLibrary.SERVER_CONFIG.walkie_talkie.speakingRange;
        listener.range = SimpleRadioLibrary.SERVER_CONFIG.walkie_talkie.listeningRange;
        speaker.category = CommonRadioPlugin.WALKIES_CATEGORY;

        transmitter.frequencingType(SimpleRadioFrequencing.WALKIE_TALKIE);
        receiver.frequencingType(SimpleRadioFrequencing.WALKIE_TALKIE);

        listener.link = this.getClass();
        speaker.link = this.getClass();
        receiver.link = this.getClass();
        transmitter.link = this.getClass();

        // --- Half-duplex implementation

        receiver.receiveCriteria(((source, radioRouter) -> {
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
        return 60;
    }
}
