package com.codinglitch.simpleradio.compat.create;

import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.Routing;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.*;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public class CentralMovementBehaviour implements MovementBehaviour {
    public void updateRouter(MovementContext context, RadioRouter router, WorldlyPosition newLocation, AbstractContraptionEntity contraption) {
        if (router == null) return;

        if (router.owner != contraption) {
            router.owner = contraption;
        }

        if (context.world.isClientSide) {
            Vector3f translatedNorth = context.rotation.apply(new Vec3(0, 0, -1)).toVector3f();
            if (router.rotation == null) router.rotation = new Quaternionf();

            // theres probably a better way to do this but im stupid so this works for now
            router.rotation.setAngleAxis(translatedNorth.angle(new Vector3f(0, 0, -1)) * -Math.signum(translatedNorth.x), 0, 1, 0);
        }

        router.location.x = newLocation.x;
        router.location.y = newLocation.y;
        router.location.z = newLocation.z;
        router.updateLocation(newLocation);
    }

    public void update(MovementContext context) {
        AbstractContraptionEntity contraptionEntity = context.contraption.entity;
        if (contraptionEntity == null) return;

        if (!(context.state.getBlock() instanceof Routing routing)) return;

        if (context.blockEntityData.contains("uuid")) {
            UUID id = context.blockEntityData.getUUID("uuid");

            WorldlyPosition newLocation;
            if (context.world.isClientSide) {
                double partialTick = AnimationTickHolder.getPartialTicks();
                Vec3 pos = context.position.add(context.motion.scale(partialTick));

                newLocation = WorldlyPosition.of(pos.toVector3f(), context.world);
            } else {
                newLocation = WorldlyPosition.of(context.position.toVector3f(), context.world);
            }


            Frequency frequency = Frequency.fromTag(context.blockEntityData);
            if (frequency != null) {
                updateRouter(context, routing.getOrCreateReceiver(newLocation, frequency, id, context.state), newLocation, contraptionEntity);
                updateRouter(context, routing.getOrCreateTransmitter(newLocation, frequency, id, context.state), newLocation, contraptionEntity);
            }

            updateRouter(context, routing.getOrCreateSpeaker(newLocation, id, context.state), newLocation, contraptionEntity);
            updateRouter(context, routing.getOrCreateListener(newLocation, id, context.state), newLocation, contraptionEntity);

            updateRouter(context, routing.getOrCreateRouter(newLocation, id, context.state), newLocation, contraptionEntity);
        }
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        MovementBehaviour.super.visitNewPosition(context, pos);

        if (context.world.isClientSide()) return;
        update(context);
    }

    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);

        if (context.world.isClientSide()) return;
        update(context);
    }

    @Override
    public void startMoving(MovementContext context) {
        MovementBehaviour.super.startMoving(context);
    }

    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        MovementBehaviour.super.renderInContraption(context, renderWorld, matrices, buffer);

        AbstractContraptionEntity contraptionEntity = context.contraption.entity;
        if (contraptionEntity == null) return;

        update(context);
    }

}
