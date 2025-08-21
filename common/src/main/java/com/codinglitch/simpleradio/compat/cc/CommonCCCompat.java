package com.codinglitch.simpleradio.compat.cc;

import com.codinglitch.simpleradio.central.Socket;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CommonCCCompat { // thats a lot of Cs
    public static final Map<BlockEntity, SocketPeripheral<?>> SOCKET_PERIPHERALS = new ConcurrentHashMap<>();

    public static void putPeripheral(BlockEntity blockEntity, IPeripheral peripheral) {
        if (blockEntity instanceof Socket) {
            SOCKET_PERIPHERALS.put(blockEntity, (SocketPeripheral<?>) peripheral);
        }
    }

    public static void removePeripheral(BlockEntity blockEntity) {
        if (blockEntity instanceof Socket) {
            SOCKET_PERIPHERALS.remove(blockEntity);
        }
    }
}
