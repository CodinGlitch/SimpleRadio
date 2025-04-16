package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.core.central.Packeter;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.platform.Services;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.radio.RadioRouter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.UUID;

public record ServerboundRequestRouterPacket(UUID reference, String type, short mapping) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "request_router");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.reference);
        buffer.writeUtf(this.type);
        buffer.writeShort(this.mapping);
    }

    public static ServerboundRequestRouterPacket decode(FriendlyByteBuf buffer) {
        return new ServerboundRequestRouterPacket(buffer.readUUID(), buffer.readUtf(), buffer.readShort());
    }

    public void handle(MinecraftServer server, ServerPlayer player) {
        UUID reference = this.reference();
        String type = this.type();
        short mapping = this.mapping();

        server.execute(() -> {
            short identifier = RadioManager.getIdentifier(r -> reference.equals(r.reference) && r.getClass().getSimpleName().equals(type));
            if (identifier == Short.MAX_VALUE) {
                CommonSimpleRadio.warn("We could not find the {} with reference {} for mapping {}!", type, reference, mapping);
                return;
            }

            Services.NETWORKING.sendToPlayer(player, new ClientboundRegisterRouterPacket(
                    mapping, identifier
            ));
        });
    }
}