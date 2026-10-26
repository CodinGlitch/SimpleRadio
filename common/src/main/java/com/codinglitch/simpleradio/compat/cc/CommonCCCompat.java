package com.codinglitch.simpleradio.compat.cc;

import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.client.ClientRadioManager;
import com.codinglitch.simpleradio.core.events.BufferEvent;
import com.codinglitch.simpleradio.radio.Message;
import com.codinglitch.simpleradio.routers.Router;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.server.TickTask;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.Optional;
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

    public static Optional<SocketPeripheral<?>> getPeripheral(Router router) {
        WorldlyPosition position = router.getPosition();
        if (position == null) return Optional.empty();

        BlockEntity blockEntity = position.getBlockEntity();
        if (blockEntity instanceof Socket) {
            return Optional.ofNullable(SOCKET_PERIPHERALS.get(blockEntity));
        }

        return Optional.empty();
    }

    public static void acceptSource(Router router, Message message) {
        getPeripheral(router).ifPresent(peripheral -> peripheral.accept(router, message));
    }

    public static void emptyBuffer(BufferEvent.Emptied event) {
        getPeripheral(event.router).ifPresent(peripheral -> peripheral.emptied(event.router));
    }

    public static void postInitialize() {
        ClientRadioManager.getInstance().listen(BufferEvent.Emptied.class, (event) -> {
            // have to force it to run tick task because it thinks its the same thread?
            event.server.tell(new TickTask(event.server.getTickCount(), () -> emptyBuffer(event)));
        });
    }
}
