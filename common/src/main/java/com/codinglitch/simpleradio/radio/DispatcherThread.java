package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.events.BufferEvent;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Thread to deliver {@link Message}s at a fixed rate to achieve a determined sample rate.
 */
public class DispatcherThread extends Thread {
    private static long FRAME_SIZE = 960;
    private static long SAMPLE_RATE = 48000;

    // Amount of padding in nanoseconds to prevent some artifacts
    private static long PADDING = 240000;

    final MinecraftServer server;

    public DispatcherThread(MinecraftServer server) {
        super("Simple Radio Dispatcher");
        this.server = server;
    }

    @Override
    public void run() {
        double rate = (double) SAMPLE_RATE / FRAME_SIZE;
        long interval = (long) (1_000_000_000L / rate);

        CommonSimpleRadio.info("Starting dispatcher!");
        CommonSimpleRadio.info("The current interval is {} nanoseconds, which means messages will be delivered {} times per second!", interval, rate);

        interval -= PADDING;

        while (!Thread.currentThread().isInterrupted()) {
            try {
                long current = System.nanoTime();

                // go through all the routers and deliver their messages for the messaging thread
                for (Router router : RadioManager.routers.values()) {
                    Collection<Queue<Message>> buffers = router.getPending().values();
                    if (buffers.isEmpty()) continue;

                    // Remove empty buffers before pulling so empty buffers have a window to re-add messages before being removed
                    buffers.removeIf(Queue::isEmpty);

                    List<Message> deliveries = ((RadioRouter) router).extract();
                    server.execute(() -> {
                        for (Message message : deliveries) {
                            if (message == null) continue;
                            router.route(message);
                        }
                    });

                    // Notify others of emptied buffer
                    for (Queue<Message> queue : buffers) {
                        if (!queue.isEmpty()) continue;
                        RadioManager.post(new BufferEvent.Emptied(router, server));
                    }
                }

                long timeTook = System.nanoTime()-current;
                long timeToWait = interval-timeTook;
                Thread.sleep(TimeUnit.NANOSECONDS.toMillis(timeToWait), (int)(timeToWait % 1_000_000L));

            } catch (InterruptedException e) {
                // thread interrupted
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void reload(long sampleRate) {
        this.interrupt();
        SAMPLE_RATE = sampleRate;
        this.start();
    }
}
