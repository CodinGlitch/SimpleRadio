package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.Socket;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.blocks.SocketBlockEntity;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.concurrent.atomic.AtomicBoolean;

public class WireItem extends Item implements WorldTicking {

    public WireItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        BlockEntity blockEntity = context.getLevel().getBlockEntity(pos);
        if (blockEntity instanceof Socket interactingSocket) {
            CommonSimpleRadio.info(interactingSocket.getReference());

            if (!interactingSocket.canConnect()) return super.useOn(context);

            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("connectTo")) {
                BlockPos connectTo = BlockPos.of(tag.getLong("connectToPos"));

                BlockEntity connectToBlockEntity = level.getBlockEntity(connectTo);
                if (connectToBlockEntity instanceof Socket socket) {

                    if (!level.isClientSide()) {
                        Wire wire = Wire.connect(interactingSocket, socket, level);

                        //connecting.connectTo(centralBlockEntity);
                        level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.PLAYERS, 1.0f, 0.8f);
                    } else {
                        if (connectToBlockEntity instanceof SocketBlockEntity socketBlockEntity) {
                            socketBlockEntity.connector = null;
                        }
                    }

                    tag.remove("connectTo");
                    tag.remove("connectToPos");

                    return InteractionResult.SUCCESS;
                }
            } else {
                tag.putUUID("connectTo", interactingSocket.getReference());
                tag.putLong("connectToPos", blockEntity.getBlockPos().asLong());

                if (level.isClientSide() && blockEntity instanceof SocketBlockEntity socket) {
                    socket.connector = context.getPlayer();
                }

                level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.PLAYERS, 1.0f, 1.1f);
                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean b) {
        super.inventoryTick(stack, level, entity, slot, b);

        AtomicBoolean isHolding = new AtomicBoolean(false);
        entity.getHandSlots().forEach(handStack -> {
            if (handStack.equals(stack)) {
                isHolding.set(true);
            }
        });

        if (!isHolding.get()) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("connectTo")) {
                BlockPos connectTo = BlockPos.of(tag.getLong("connectToPos"));

                BlockEntity connectToBlockEntity = level.getBlockEntity(connectTo);
                if (level.isClientSide && connectToBlockEntity instanceof SocketBlockEntity socketBlockEntity) {
                    socketBlockEntity.connector = null;
                }

                tag.remove("connectTo");
            }
        }
    }

    @Override
    public void worldTick(ItemEntity item, Level level) {
        CompoundTag tag = item.getItem().getOrCreateTag();
        if (tag.contains("connectTo")) {
            tag.remove("connectTo");
        }
    }
}
