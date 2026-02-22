package com.codinglitch.simpleradio.client.core;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundActivityPacket;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundRegisterRouterPacket;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundSpeakSoundPacket;
import com.codinglitch.simpleradio.core.networking.packets.ClientboundWireEffectPacket;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.joml.Math;

public class SimpleRadioClientNetworking {

    public static void handleActivityPacket(ClientboundActivityPacket packet) {
        float activity = packet.activity();
        short identifier = packet.identifier();

        Minecraft.getInstance().execute(() -> {
            RadioRouter router = (RadioRouter) ClientRadioManager.getInstance().getRouter(identifier);
            if (router == null) return;

            router.activity = activity;
            router.activityTime = SimpleRadioLibrary.SERVER_CONFIG.router.activityTime;
        });
    }

    public static void handleRegisterRouter(ClientboundRegisterRouterPacket packet) {
        short mapping = packet.mapping();
        short identifier = packet.identifier();

        Minecraft.getInstance().execute(() -> ClientRadioManager.finalizeRouter(mapping, identifier));
    }

    public static void handleSpeakSound(ClientboundSpeakSoundPacket packet) {
        Minecraft.getInstance().execute(() -> {
            ClientRadioManager.speakSound(packet);
        });
    }

    public static void handleWireEffect(ClientboundWireEffectPacket packet) {
        int id = packet.entityId();
        boolean reversed = packet.reversed();

        Minecraft.getInstance().execute(() -> {
            if (!SimpleRadioLibrary.CLIENT_CONFIG.wire.effect) return;

            Entity entity = Minecraft.getInstance().level.getEntity(id);
            if (entity instanceof Wire wire) {
                Wire.Effect effect = new Wire.Effect();
                if (reversed) {
                    effect.progress = Math.round(SimpleRadioLibrary.CLIENT_CONFIG.wire.effectTime * wire.getLength());
                    effect.direction = -1;
                } else {
                    effect.progress = 0;
                    effect.direction = 1;
                }
                wire.effectList.add(effect);
            }
        });
    }
}
