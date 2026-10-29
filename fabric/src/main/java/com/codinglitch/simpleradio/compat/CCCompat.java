package com.codinglitch.simpleradio.compat;

import com.codinglitch.intermediator.Intermediation;
import com.codinglitch.intermediator.phases.PostInitializePhase;
import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.compat.cc.CommonCCCompat;
import com.codinglitch.simpleradio.compat.cc.SocketPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;

public class CCCompat extends CommonCCCompat {

    @Override
    public void postInitialize(PostInitializePhase data) {
        PeripheralLookup.get().registerFallback(((level, blockPos, blockState, blockEntity, direction) -> {
            if (blockEntity instanceof Socket) {
                return new SocketPeripheral<>(blockEntity);
            }
            return null;
        }));

        super.postInitialize(data);
    }

    @Override
    public void mediate(Intermediation intermediation) {
        super.mediate(intermediation);
    }
}
