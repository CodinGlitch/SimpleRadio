package com.codinglitch.simpleradio.compat.cc;

import com.codinglitch.intermediator.Intermediation;
import com.codinglitch.intermediator.Mediation;
import com.codinglitch.intermediator.Subscriber;
import com.codinglitch.intermediator.phases.PostInitializePhase;
import com.codinglitch.simpleradio.central.Socket;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.compat.phases.BlockEntityRemovedPhase;
import com.codinglitch.simpleradio.core.events.BufferEvent;
import com.codinglitch.simpleradio.core.events.RouterEvent;
import com.codinglitch.simpleradio.routers.Router;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.server.TickTask;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class CommonCCCompat implements Mediation { // thats a lot of Cs
    public static final Map<BlockEntity, SocketPeripheral<?>> SOCKET_PERIPHERALS = new ConcurrentHashMap<>();

    public static void putPeripheral(BlockEntity blockEntity, IPeripheral peripheral) {
        if (blockEntity instanceof Socket) {
            SOCKET_PERIPHERALS.put(blockEntity, (SocketPeripheral<?>) peripheral);
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

    public void acceptMessage(RouterEvent.Messaged event) {
        getPeripheral(event.router).ifPresent(peripheral -> peripheral.accept(event.router, event.message));
    }

    public void emptyBuffer(BufferEvent.Emptied event) {
        // have to force it to run tick task because it thinks its the same thread?
        event.server.tell(new TickTask(
                event.server.getTickCount(),
                () -> getPeripheral(event.router).ifPresent(peripheral -> peripheral.emptied(event.router))
        ));
    }

    public void postInitialize(PostInitializePhase data) {

    }

    public void blockEntityRemoved(BlockEntityRemovedPhase data) {
        if (data.blockEntity instanceof Socket) {
            SOCKET_PERIPHERALS.remove(data.blockEntity);
        }
    }

    @Override
    public void mediate(Intermediation intermediation) {
        intermediation.subscribe(PostInitializePhase.class, Subscriber.Condition.ENABLED, this::postInitialize);
        intermediation.subscribe(BufferEvent.Emptied.class, Subscriber.Condition.ENABLED, this::emptyBuffer);
        intermediation.subscribe(RouterEvent.Messaged.class, Subscriber.Condition.LOADED, this::acceptMessage);
        intermediation.subscribe(BlockEntityRemovedPhase.class, Subscriber.Condition.LOADED, this::blockEntityRemoved);
    }
}
