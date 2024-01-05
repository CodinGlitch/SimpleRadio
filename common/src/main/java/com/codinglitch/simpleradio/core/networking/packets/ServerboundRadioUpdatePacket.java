package com.codinglitch.simpleradio.core.networking.packets;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.Transceiving;
import com.codinglitch.simpleradio.core.networking.Packeter;
import com.codinglitch.simpleradio.core.registry.blocks.RadiosmitherBlockEntity;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public record ServerboundRadioUpdatePacket(String frequency, Frequency.Modulation modulation) implements Packeter {
    public static ResourceLocation ID = new ResourceLocation(CommonSimpleRadio.ID, "radio_update_packet");
    @Override
    public ResourceLocation resource() {
        return ID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.frequency);
        buffer.writeUtf(this.modulation.shorthand);
    }

    public static ServerboundRadioUpdatePacket decode(FriendlyByteBuf buffer) {
        return new ServerboundRadioUpdatePacket(buffer.readUtf(), Frequency.modulationOf(buffer.readUtf()));
    }

    public void handle(MinecraftServer server, ServerPlayer player) {
        server.execute(() -> {
            if (!Frequency.validate(frequency)) return;

            AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof RadiosmitherMenu radiosmitherMenu) {
                if (!player.containerMenu.stillValid(player)) {
                    CommonSimpleRadio.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                    return;
                }

                radiosmitherMenu.updateTinkering(frequency, modulation);
            }
        });
    }
}