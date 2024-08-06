package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.core.central.AuditoryBlockEntity;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
        if (blockEntity instanceof AuditoryBlockEntity centralBlockEntity) {
            if (!centralBlockEntity.canConnect()) return super.useOn(context);

            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("connectTo")) {
                BlockPos connectTo = BlockPos.of(tag.getLong("connectTo"));

                BlockEntity connectToBlockEntity = level.getBlockEntity(connectTo);
                if (connectToBlockEntity instanceof AuditoryBlockEntity connecting) {

                    if (!level.isClientSide()) {
                        connecting.connectTo(centralBlockEntity);
                    }

                    tag.remove("connectTo");
                }
            } else {
                tag.putLong("connectTo", centralBlockEntity.getBlockPos().asLong());
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
