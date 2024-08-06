package com.codinglitch.simpleradio.compat.create;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.*;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

public class CentralMovementBehaviour implements MovementBehaviour {
    @Override
    public void startMoving(MovementContext context) {
        MovementBehaviour.super.startMoving(context);

        CommonSimpleRadio.info("start moving");
    }

    public void updateRouter(RadioRouter router, WorldlyPosition newLocation, AbstractContraptionEntity contraption) {
        if (router == null) return;

        if (router.owner != contraption) {
            router.owner = contraption;
        }

        router.location = newLocation;
        router.updateLocation(newLocation);
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        MovementBehaviour.super.visitNewPosition(context, pos);
    }

    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);

        AbstractContraptionEntity contraptionEntity = context.contraption.entity;
        if (contraptionEntity == null) return;
        if (context.world.isClientSide()) return;

        if (context.motion.length() <= 0d) return;

        if (context.blockEntityData.contains("uuid")) {
            UUID id = context.blockEntityData.getUUID("uuid");
            WorldlyPosition newLocation = WorldlyPosition.of(context.position.toVector3f(), context.world);

            Frequency frequency = Frequency.fromTag(context.blockEntityData);
            if (frequency != null) {
                updateRouter(frequency.getReceiver(id), newLocation, contraptionEntity);
                updateRouter(frequency.getTransmitter(id), newLocation, contraptionEntity);
            }

            updateRouter(RadioSpeaker.getSpeaker(id), newLocation, contraptionEntity);
            updateRouter(RadioListener.getListener(id), newLocation, contraptionEntity);
        }
    }

    @Override
    public boolean renderAsNormalBlockEntity() {
        return true;
    }
}
