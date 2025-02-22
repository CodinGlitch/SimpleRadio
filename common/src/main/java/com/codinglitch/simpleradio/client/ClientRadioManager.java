package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.client.core.ClientRouterWrapper;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class ClientRadioManager {
    private static final List<ClientRouterWrapper> routers = new ArrayList<>();

    public static List<RadioRouter> getRouters() {
        return routers.stream().map(wrapper -> wrapper.router).toList();
    }

    public static RadioRouter getRouter(Predicate<RadioRouter> criteria) {
        ClientRouterWrapper routerWrapper = routers.stream().filter(wrapper -> criteria.test(wrapper.router)).findFirst().orElse(null);
        if (routerWrapper == null) return null;

        return routerWrapper.router;
    }

    // im, losing it

    public static ClientRouterWrapper getWrapper(UUID uuid) {
        return routers.stream().filter(wrapper -> wrapper.router.id.equals(uuid)).findFirst().orElse(null);
    }

    public static RadioRouter getRouter(UUID uuid) {
        return ClientRadioManager.getRouter(router -> uuid.equals(router.id));
    }
    public static RadioRouter getRouter(Entity owner) {
        return ClientRadioManager.getRouter(router -> owner.equals(router.owner));
    }
    public static RadioRouter getRouter(WorldlyPosition location) {
        return ClientRadioManager.getRouter(router -> location.equals(router.location));
    }

    public static RadioListener getListener(UUID uuid) {
        return (RadioListener) ClientRadioManager.getRouter(router -> uuid.equals(router.id) && router instanceof RadioListener);
    }
    public static RadioListener getListener(Entity owner) {
        return (RadioListener) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioListener);
    }
    public static RadioListener getListener(WorldlyPosition location) {
        return (RadioListener) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioListener);
    }

    public static RadioSpeaker getSpeaker(UUID uuid) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> uuid.equals(router.id) && router instanceof RadioSpeaker);
    }
    public static RadioSpeaker getSpeaker(Entity owner) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioSpeaker);
    }
    public static RadioSpeaker getSpeaker(WorldlyPosition location) {
        return (RadioSpeaker) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioSpeaker);
    }

    public static RadioReceiver getReceiver(UUID uuid) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> uuid.equals(router.id) && router instanceof RadioReceiver);
    }
    public static RadioReceiver getReceiver(Entity owner) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioReceiver);
    }
    public static RadioReceiver getReceiver(WorldlyPosition location) {
        return (RadioReceiver) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioReceiver);
    }

    public static RadioTransmitter getTransmitter(UUID uuid) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> uuid.equals(router.id) && router instanceof RadioTransmitter);
    }
    public static RadioTransmitter getTransmitter(Entity owner) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> owner.equals(router.owner) && router instanceof RadioTransmitter);
    }
    public static RadioTransmitter getTransmitter(WorldlyPosition location) {
        return (RadioTransmitter) ClientRadioManager.getRouter(router -> location.equals(router.location) && router instanceof RadioTransmitter);
    }

    public static void registerRouter(RadioRouter router) {
        routers.add(ClientRouterWrapper.of(router));
    }
    public static void removeRouter(RadioRouter router) {
        routers.removeIf(wrapper -> wrapper.router == router);
    }
    public static void removeRouter(UUID uuid) {
        routers.removeIf(wrapper -> uuid.equals(wrapper.router.id));
    }
    public static void removeRouter(Entity owner) {
        routers.removeIf(wrapper -> owner.equals(wrapper.router.owner));
    }
    public static void removeRouter(WorldlyPosition location) {
        routers.removeIf(wrapper -> wrapper.router.location != null && location.equals(wrapper.router.location));
    }

    public static void garbageCollect() {
        routers.removeIf(wrapper -> !wrapper.router.validate());
        routers.removeIf(wrapper -> wrapper.router.owner == null && wrapper.router.location == null);
    }

    public static void tick(long gameTime) {
        if (gameTime % 20 == 0) {
            garbageCollect();
        }

        for (RadioRouter router : getRouters()) {
            router.tick(0);
        }
    }

    public static void close() {
        routers.clear();
    }



    //

    public static void renderRouter(RadioRouter router, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vector3f camera) {
        poseStack.pushPose();

        Vector3f location = null;
        if (router.location != null) {
            location = new Vector3f(router.location.x, router.location.y, router.location.z);
        } else if (router.owner != null) {
            location = router.owner.position().toVector3f();
        }

        if (location == null) return;

        float r = 0.1f;
        float g = 0.1f;
        float b = 0.1f;

        if (router instanceof RadioListener) {
            r = 1f;
            g = 1f;
        } else if (router instanceof RadioSpeaker) {
            r = 1f;
            b = 1f;
        } else if (router instanceof RadioReceiver) {
            r = 1f;
        } else if (router instanceof RadioTransmitter) {
            b = 1f;
        } else {
            g = 1f;
        }

        location = location.sub(camera);
        poseStack.translate(location.x, location.y, location.z);

        if (router.rotation != null) {
            poseStack.mulPose(router.rotation);
        }

        Vector3f newOffset;
        if (router.connectionOffset == Vec3.ZERO) {
            newOffset = new Vector3f();
        } else {
            newOffset = router.connectionOffset.toVector3f();
        }

        //Vec3 newLocation = location.getCenter().add(new Vec3(newOffset));
        AABB pointBox = new AABB(
                -0.05f, -0.05f, -0.05f,
                0.05f, 0.05f, 0.05f
        ).move(newOffset.x, newOffset.y, newOffset.z);
        DebugRenderer.renderFilledBox(poseStack, bufferSource, pointBox, r, g, b, 0.8f);

        AABB boundingBox = new AABB(
                -0.5f, -0.5f, -0.5f,
                0.5f, 0.5f, 0.5f
        );
        LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), boundingBox, r, g, b, 0.8f);

        poseStack.popPose();
    }
}
