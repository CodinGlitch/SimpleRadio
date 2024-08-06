package com.codinglitch.simpleradio.compat.create;

import com.codinglitch.simpleradio.core.central.*;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlocks;
import com.codinglitch.simpleradio.radio.*;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class CreateCompat {
    public static void contraptionAddBlock(Contraption contraption, BlockPos pos, BlockEntity blockEntity, StructureTemplate.StructureBlockInfo info) {
        if (blockEntity instanceof AuditoryBlockEntity centralBlockEntity) {
            centralBlockEntity.receiver = null;
            centralBlockEntity.transmitter = null;
            centralBlockEntity.speaker = null;
            centralBlockEntity.listener = null;

            centralBlockEntity.frequency = null;
        }
    }

    public static void registerMovementBehaviours() {
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.RADIO, new CentralMovementBehaviour());
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.SPEAKER, new CentralMovementBehaviour());
        AllMovementBehaviours.registerBehaviour(SimpleRadioBlocks.MICROPHONE, new CentralMovementBehaviour());
    }

    public static RadioManager.CollectionResult verifyContraptionCollection(Entity entity) {
        if (entity instanceof AbstractContraptionEntity contraptionEntity) {
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
