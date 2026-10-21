package com.codinglitch.simpleradio.core.events;

import com.codinglitch.simpleradio.core.RadioEvent;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.server.MinecraftServer;

/**
 * Events relating to the buffer in a {@link Router}.
 */
public class BufferEvent extends RadioEvent {

    /**
     * Called immediately after the last message in a buffer is emptied.
     * <br><br>
     * This event is called on the courier thread; use the server to execute code on the main thread.
     */
    public static class Emptied extends RadioEvent {
        public final Router router;
        public final MinecraftServer server;

        public Emptied(Router router, MinecraftServer server) {
            this.router = router;
            this.server = server;
        }
    }

}
