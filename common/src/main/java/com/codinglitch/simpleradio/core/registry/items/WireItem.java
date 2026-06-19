package com.codinglitch.simpleradio.core.registry.items;

import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.central.Wiring;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.central.WorldTicking;
import com.codinglitch.simpleradio.core.registry.blocks.InsulatorBlockEntity;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3f;

import java.util.Optional;
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
        Player player = context.getPlayer();

        BlockEntity blockEntity = context.getLevel().getBlockEntity(pos);
        if (blockEntity instanceof Socket interactingSocket) {
            if (!interactingSocket.canConnect()) return super.useOn(context);

            // Disconnecting functionality
            if (player != null && player.isCrouching()) {

                Router router = interactingSocket.getRouter();
                if (router == null) return super.useOn(context);

                WorldlyPosition origin = router.getLocation();
                Vector3f look = player.getLookAngle().toVector3f();

                // Sort the wires by their direction; the one that most closely matches where the player is looking will be chosen first
                Optional<Wiring> bestWire = interactingSocket
                        .getWires().stream().min((first, second) -> {
                            Router firstOpposite = first.transport(interactingSocket);
                            Router secondOpposite = second.transport(interactingSocket);

                            if (firstOpposite == null) return 0;
                            if (secondOpposite == null) return 0;

                            float firstScore = origin.sub(firstOpposite.getLocation(), new Vector3f()).normalize().dot(look);
                            float secondScore = origin.sub(secondOpposite.getLocation(), new Vector3f()).normalize().dot(look);

                            return (int) ((secondScore - firstScore) * 10);
                        });

                if (bestWire.isPresent()) {
                    bestWire.get().burnOut();
                    return InteractionResult.SUCCESS;
                }

            }

            // Connecting functionality
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("connectTo")) {
                BlockPos connectTo = BlockPos.of(tag.getLong("connectToPos"));

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

                    tag.remove("connectTo");
                    tag.remove("connectToPos");

                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            } else {
                tag.putUUID("connectTo", interactingSocket.getReference());
                tag.putLong("connectToPos", blockEntity.getBlockPos().asLong());

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
                if (connectToBlockEntity instanceof InsulatorBlockEntity insulatorBlockEntity) {
                    insulatorBlockEntity.removeConnector();
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
