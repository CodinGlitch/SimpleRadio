package com.codinglitch.simpleradio.compat.create;

import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.blocks.AuditoryBlockEntity;
import com.codinglitch.simpleradio.core.registry.blocks.SocketBlockEntity;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.*;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateCompat {
    public static List<Block> CENTRAL_BLOCKS = List.of(
        SimpleRadioBlocks.RADIO,
        SimpleRadioBlocks.SPEAKER,
        SimpleRadioBlocks.MICROPHONE,
        SimpleRadioBlocks.RECEIVER,
        SimpleRadioBlocks.TRANSMITTER,
        SimpleRadioBlocks.SOCKET
    );

    public static void contraptionAddBlock(Contraption contraption, BlockPos pos, BlockEntity blockEntity, StructureTemplate.StructureBlockInfo info) {
        if (blockEntity instanceof AuditoryBlockEntity centralBlockEntity) {
            centralBlockEntity.receiver = null;
            centralBlockEntity.transmitter = null;
            centralBlockEntity.speaker = null;
            centralBlockEntity.listener = null;

            centralBlockEntity.frequency = null;
        } else if (blockEntity instanceof SocketBlockEntity socketBlockEntity) {
            socketBlockEntity.router = null;
        }
    }

    private static void resetRouter(RadioRouter router, BlockPos pos, Level level) {
        if (router == null) return;
        router.owner = null;
        router.location = Services.COMPAT.modifyPosition(WorldlyPosition.of(pos, level, pos));
    }

    public static void contraptionRemoveBlock(Contraption contraption, Level level, BlockPos pos, BlockState state, CompoundTag tag) {
        if (tag.contains("uuid")) {
            UUID uuid = tag.getUUID("uuid");

            if (level.isClientSide) {
                resetRouter(ClientRadioManager.getReceiver(uuid), pos, level);
                resetRouter(ClientRadioManager.getTransmitter(uuid), pos, level);

                resetRouter(ClientRadioManager.getListener(uuid), pos, level);
                resetRouter(ClientRadioManager.getSpeaker(uuid), pos, level);

                // might be problematic
                resetRouter(ClientRadioManager.getRouter(router -> {
                    return uuid.equals(router.getID()) && router.getClass() == RadioRouter.class;
                }), pos, level);
            } else {
                resetRouter(RadioRouter.getRouterFromReceivers(uuid), pos, level);
                resetRouter(RadioRouter.getRouterFromTransmitters(uuid), pos, level);

                resetRouter(RadioManager.getListener(uuid), pos, level);
                resetRouter(RadioManager.getSpeaker(uuid), pos, level);

                resetRouter(RadioRouter.getRouterFromUUID(uuid, null), pos, level);
            }

        }
    }

    public static void registerMovementBehaviours() {
        for (Block centralBlock : CENTRAL_BLOCKS) {
            if (MovementBehaviour.REGISTRY.get(centralBlock) != null) continue;
            MovementBehaviour.REGISTRY.register(centralBlock, new CentralMovementBehaviour());
        }
    }

    public static RadioManager.CollectionResult verifyContraptionCollection(Entity entity) {
        if (entity instanceof AbstractContraptionEntity contraptionEntity) {
            if (contraptionEntity.isRemoved()) return RadioManager.CollectionResult.COLLECT;

            Contraption contraption = contraptionEntity.getContraption();
            if (contraption != null) {
                return contraption.disassembled ? RadioManager.CollectionResult.COLLECT : RadioManager.CollectionResult.IGNORE;
            } else {
                return RadioManager.CollectionResult.COLLECT;
            }
        } else {
            return RadioManager.CollectionResult.PASS;
        }
    }
}
