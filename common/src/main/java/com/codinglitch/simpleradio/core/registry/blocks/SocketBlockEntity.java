package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.Socket;
import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SocketBlockEntity extends BlockEntity implements Socket {

    UUID id;

    public RadioRouter router;

    public SocketBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.SOCKET, pos, state);
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
        if (router != null) {
            RadioManager.removeRouterSided(router, this.level.isClientSide);
        }

        super.setRemoved();
    }

    @Override
    public RadioRouter getRouter() {
        return router;
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("uuid")) {
            this.id = tag.getUUID("uuid");
        }

        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (this.id != null) {
            tag.putUUID("uuid", this.id);
        }

        super.saveAdditional(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SocketBlockEntity blockEntity) {
        if (blockEntity.router == null && blockEntity.id != null) {
            WorldlyPosition location = Services.COMPAT.modifyPosition(WorldlyPosition.of(pos, level, pos));

            blockEntity.router = SimpleRadioBlocks.SOCKET.getOrCreateRouter(location, blockEntity.id, state);
        }
    }
}
