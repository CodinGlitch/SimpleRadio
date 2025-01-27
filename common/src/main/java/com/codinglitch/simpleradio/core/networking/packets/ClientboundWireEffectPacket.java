package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.joml.Math;

public record ClientboundWireEffectPacket(int id, boolean reversed) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "wire_effect_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id);
        buffer.writeBoolean(this.reversed);
    }

    public static ClientboundWireEffectPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundWireEffectPacket(buffer.readVarInt(), buffer.readBoolean());
    }

    public static void handle(ClientboundWireEffectPacket packet) {
        int id = packet.id();
        boolean reversed = packet.reversed();

        Minecraft.getInstance().execute(() -> {
            if (!SimpleRadioLibrary.CLIENT_CONFIG.wire.effect) return;

            Entity entity = Minecraft.getInstance().level.getEntity(id);
            if (entity instanceof Wire wire) {
                Wire.Effect effect = new Wire.Effect();
                if (reversed) {
                    effect.progress = (int) Math.round(SimpleRadioLibrary.CLIENT_CONFIG.wire.effectTime * wire.getLength());
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