package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioApi;
import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.SimpleRadioSounds;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class InsulatorBlockEntity extends BlockEntity implements Socket {

    public UUID id;
    public RadioRouter router;

    public float rotation = 0;
    public Player connector;

    public InsulatorBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.INSULATOR, pos, state);
    }

    public void setConnector(Player connector) {
        this.connector = connector;

        if (this.hasLevel() && !this.level.isClientSide) {
            this.level.playSound(null, this.getBlockPos(), SimpleRadioSounds.SPIN_INSULATOR, SoundSource.BLOCKS, 0.5f, 0.9f + this.level.random.nextFloat()*0.2f);
        }
    }

    public void removeConnector() {
        this.connector = null;

        if (this.hasLevel() && !this.level.isClientSide) {
            this.level.playSound(null, this.getBlockPos(), SimpleRadioSounds.SPIN_INSULATOR, SoundSource.BLOCKS, 0.5f, 0.9f + this.level.random.nextFloat()*0.2f);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (this.id == null && !level.isClientSide) {
            this.id = UUID.randomUUID();
        }
    }

    @Override
    public void setRemoved() {
        this.deactivate();
        super.setRemoved();
    }

    @Override
    public RadioRouter getRouter() {
        return router != null ? router : (this.hasLevel() ? (RadioRouter) SimpleRadioApi.getRouterSided(this.id, this.level.isClientSide) : null);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("reference")) {
            this.id = tag.getUUID("reference");
        }

        super.loadAdditional(tag, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        if (this.id != null) {
            tag.putUUID("reference", this.id);
        }

        super.saveAdditional(tag, provider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InsulatorBlockEntity blockEntity) {
        if (blockEntity.router != null && !blockEntity.router.isValid()) {
            blockEntity.deactivate();
        }

        if (blockEntity.router == null && blockEntity.id != null) {
            blockEntity.activate();
        }
    }

    public void activate() {
        CommonSimpleRadio.info("Activating insulator with reference {}", id);
        WorldlyPosition location = CompatCore.modifyPosition(WorldlyPosition.of(worldPosition, level, worldPosition));

        this.router = (RadioRouter) SimpleRadioBlocks.INSULATOR.getOrCreateRouter(location, id, this.getBlockState());
    }
    public void deactivate() {
        if (router != null) {
            SimpleRadioApi.removeRouterSided(router, this.level.isClientSide);
            this.router = null;
        }
    }
}
