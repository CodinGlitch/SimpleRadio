package com.codinglitch.simpleradio.compat.create;

import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.core.registry.blocks.AuditoryBlockEntity;
import com.codinglitch.simpleradio.core.registry.blocks.SocketBlockEntity;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.*;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.UUID;

public class CreateCompat {
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
        if (level.isClientSide) return;

        if (tag.contains("uuid")) {
            UUID uuid = tag.getUUID("uuid");

            resetRouter(RadioRouter.getRouterFromReceivers(uuid), pos, level);
            resetRouter(RadioRouter.getRouterFromTransmitters(uuid), pos, level);

            resetRouter(RadioManager.getListener(uuid), pos, level);
            resetRouter(RadioManager.getSpeaker(uuid), pos, level);

            resetRouter(RadioRouter.getRouterFromUUID(uuid, null), pos, level);
        }
    }

    public static void registerMovementBehaviours() {
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.RADIO, new CentralMovementBehaviour());
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.SPEAKER, new CentralMovementBehaviour());
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.MICROPHONE, new CentralMovementBehaviour());
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.RECEIVER, new CentralMovementBehaviour());
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.TRANSMITTER, new CentralMovementBehaviour());
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.SOCKET, new CentralMovementBehaviour());
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
