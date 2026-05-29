package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.central.Routing;
import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.compat.cc.SocketPeripheral;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;
import java.util.function.Predicate;

public class CCCompat {

    // best be careful, Routing does not necessarily mean Socket exists for the block entity
    private static final Predicate<Block> IS_SOCKET = block -> block instanceof Routing;

    public static void register(RegisterCapabilitiesEvent event) {
        for (Map.Entry<ResourceLocation, BlockEntityType<?>> entry : SimpleRadioBlockEntities.BLOCK_ENTITIES.entrySet()) {
            // a bit of a hacky way to check for socket; there's no way to check if the blockentity extends Socket from here (that i know of)
            // so we check the individual blocks for Routing instead. there's no guarantee this will work 100% of the time
            // of course, later on it's possible i store the class of the block entity in BLOCK_ENTITIES instead to get around this.
            BlockEntityType<?> type = entry.getValue();
            if (type.getValidBlocks().stream().anyMatch(IS_SOCKET))
                event.registerBlockEntity(PeripheralCapability.get(), type, (b, d) -> new SocketPeripheral<>(b));
        }
    }
}
