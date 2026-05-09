package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.blocks.InsulatorBlockEntity;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.codinglitch.simpleradio.core.SimpleRadioComponents.WIRE_POSITION;
import static com.codinglitch.simpleradio.core.SimpleRadioComponents.WIRE_TARGET;

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

            if (!interactingSocket.canConnect()) return super.useOn(context);

            if (stack.has(WIRE_TARGET)) {
                BlockPos connectTo = BlockPos.of(stack.get(WIRE_POSITION));

                BlockEntity connectToBlockEntity = level.getBlockEntity(connectTo);
                if (connectToBlockEntity instanceof Socket socket) {

                    if (!level.isClientSide()) {
                        Wire wire = Wire.connect(interactingSocket, socket, level);

                        //connecting.connectTo(centralBlockEntity);
                        level.playSound(null, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.PLAYERS, 1.0f, 0.8f);
                        stack.shrink(1);
                    }

                    if (connectToBlockEntity instanceof InsulatorBlockEntity insulatorBlockEntity) {
                        insulatorBlockEntity.removeConnector();
                    }

                    stack.remove(WIRE_TARGET);
                    stack.remove(WIRE_POSITION);

                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else {
                stack.set(WIRE_TARGET,   interactingSocket.getReference());
                stack.set(WIRE_POSITION, blockEntity.getBlockPos().asLong());

                if (blockEntity instanceof InsulatorBlockEntity socket) {
                    socket.setConnector(context.getPlayer());
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

        if (!(entity instanceof LivingEntity livingEntity)) return;

        AtomicBoolean isHolding = new AtomicBoolean(false);
        livingEntity.getHandSlots().forEach(handStack -> {
            if (handStack.equals(stack)) {
                isHolding.set(true);
            }
        });

        if (!isHolding.get()) {
            if (stack.has(WIRE_TARGET)) {
                BlockPos connectTo = BlockPos.of(stack.get(WIRE_POSITION));

                BlockEntity connectToBlockEntity = level.getBlockEntity(connectTo);
                if (connectToBlockEntity instanceof InsulatorBlockEntity insulatorBlockEntity) {
                    insulatorBlockEntity.removeConnector();
                }

                stack.remove(WIRE_TARGET);
            }
        }
    }

    @Override
    public void worldTick(ItemEntity item, Level level) {
        ItemStack stack = item.getItem();
        if (stack.has(WIRE_TARGET)) {
            stack.remove(WIRE_POSITION);
        }
    }
}
