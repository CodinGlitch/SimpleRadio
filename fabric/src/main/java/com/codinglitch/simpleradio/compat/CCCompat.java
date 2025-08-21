package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.compat.cc.SocketPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;

public class CCCompat {
    public static void postInitialize() {
        PeripheralLookup.get().registerFallback(((level, blockPos, blockState, blockEntity, direction) -> {
            if (blockEntity instanceof Socket) {
                return new SocketPeripheral<>(blockEntity);
            }
            return null;
        }));
    }
}
