package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.central.Receiving;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioReceiver;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ReceiverBlockEntity extends CatalyzingBlockEntity implements Receiving {
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
                    null, receiver.getPosition().x, receiver.getPosition().y, receiver.getPosition().z,
                    SimpleRadioSounds.RADIO_CLOSE,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }

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
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        loadTag(tag);

        if (tag.contains("antennaPower")) {
            this.antennaPower = tag.getInt("antennaPower");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        saveTag(tag);
        super.saveAdditional(tag, provider);
    }

    @Override
    public void markDirty() {
        this.isDirty = true;
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, ReceiverBlockEntity blockEntity) {
        if (blockEntity.frequency != null && blockEntity.id != null && !blockEntity.active) {
            blockEntity.activate();
        }
        CatalyzingBlockEntity.tick(level, pos, blockState, blockEntity);

        if (blockEntity.receiver != null) blockEntity.receiver.setActive(blockEntity.catalyst != null);

        if (!blockEntity.catalyzed) return;

        if (blockEntity.isDirty && level.getGameTime() % 200 == 0 && !level.isClientSide) {
            blockEntity.antennaPower = blockEntity.calculateAntennaPower(blockEntity.getAdaptorLocation(), level);
            Router router = blockEntity.getRouter();
            if (router instanceof RadioReceiver receiver) receiver.antennaPower = blockEntity.antennaPower;

            level.sendBlockUpdated(pos, blockState, blockState, Block.UPDATE_CLIENTS);
            blockEntity.setChanged();
            blockEntity.isDirty = false;
        }
    }

    @Override
    public void deactivate() {
        if (this.frequency != null)
            stopReceiving(frequency.getFrequency(), frequency.getModulation(), id, level.isClientSide);

        // Clean up the invalidated routers.
        super.deactivate();
    }

    @Override
    public void activate() {
        CommonSimpleRadio.info("Activating receiver with reference {}", id);
        WorldlyPosition location = CompatCore.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.receiver = SimpleRadioBlocks.RECEIVER.getOrCreateReceiver(location, frequency, id, this.getBlockState());
        if (!level.isClientSide) {
            level.playSound(
                    null, location.x, location.y, location.z,
                    SimpleRadioSounds.RADIO_OPEN,
                    SoundSource.PLAYERS,
                    1f, 1f
            );
        }

        // Mark this block as active.
        super.activate();
        markDirty();
    }

    @Override
    public int getAntennaPower() {
        return this.antennaPower;
    }
}
