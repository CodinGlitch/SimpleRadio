package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.api.central.Receiving;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ReceiverBlockEntity extends CatalyzingBlockEntity implements Receiving {
    public boolean isActive = false;
    public boolean isDirty = true;
    public int antennaPower = 0;

    public ReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.RECEIVER, pos, state);
    }

    @Override
    public BlockPos getAdaptorLocation() {
        return getBlockPos().relative(getBlockState().getValue(ReceiverBlock.FACING).getOpposite());
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && this.receiver != null) {
            level.playSound(
                    null, receiver.location.x, receiver.location.y, receiver.location.z,
                    SimpleRadioSounds.RADIO_CLOSE,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }

        inactivate();

        super.setRemoved();
    }

    @Override
    public void loadTag(CompoundTag tag) {
        //inactivate();
        super.loadTag(tag);
    }
    @Override
    public void saveTag(CompoundTag tag) {
        super.saveTag(tag);

        tag.putInt("antennaPower", antennaPower);
    }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadTag(tag);

        if (tag.contains("antennaPower")) {
            this.antennaPower = tag.getInt("antennaPower");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        saveTag(tag);
        super.saveAdditional(tag);
    }

    @Override
    public void saveToItem(ItemStack stack) {
        saveTag(stack.getOrCreateTag());
        super.saveToItem(stack);
    }

    @Override
    public void markDirty() {
        this.isDirty = true;
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, ReceiverBlockEntity blockEntity) {
        if (blockEntity.frequency != null && blockEntity.id != null && !blockEntity.isActive) {
            blockEntity.activate();
        }
        CatalyzingBlockEntity.tick(level, pos, blockState, blockEntity);

        if (!blockEntity.catalyzed) return;

        if (blockEntity.isDirty && level.getGameTime() % 200 == 0 && !level.isClientSide) {
            blockEntity.antennaPower = blockEntity.calculateAntennaPower(blockEntity.getAdaptorLocation(), level);
            RadioRouter router = blockEntity.getRouter();
            if (router instanceof RadioReceiver receiver) receiver.antennaPower = blockEntity.antennaPower;

            level.sendBlockUpdated(pos, blockState, blockState, 2);
            blockEntity.setChanged();
            blockEntity.isDirty = false;
        }
    }

    public void inactivate() {
        if (this.frequency != null) {
            RadioManager.removeRouterSided(this.id, this.getLevel().isClientSide);
            if (!this.level.isClientSide) stopReceiving(frequency.frequency, frequency.modulation, this.id);
        }

        this.isActive = false;
    }

    public void activate() {
        WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        if (!level.isClientSide) {
            this.receiver = SimpleRadioBlocks.RECEIVER.getOrCreateReceiver(location, frequency, id, this.getBlockState());

            level.playSound(
                    null, location.x, location.y, location.z,
                    SimpleRadioSounds.RADIO_OPEN,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        } else {
            this.receiver = new RadioReceiver(frequency, location, id);
            ClientRadioManager.registerRouter(receiver);
        }

        this.isActive = true;
        markDirty();
    }

    @Override
    public int getAntennaPower() {
        return this.antennaPower;
    }
}
