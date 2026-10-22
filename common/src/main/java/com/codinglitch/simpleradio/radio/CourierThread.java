package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Thread that handles the delivery of messages along routers with a delay not bounded by ticks.
 */
public class CourierThread extends Thread {

    public DelayQueue<DelayedMessage> queuedMessages = new DelayQueue<>();
    final MinecraftServer server;

    public CourierThread(MinecraftServer server) {
        super("Simple Radio Courier");
        this.server = server;
    }

    @Override
    public void run() {

        CommonSimpleRadio.info("Starting courier!");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                DelayedMessage newMessage = queuedMessages.take();

                // relay the delayed message on the main thread
                server.execute(() -> {
                    newMessage.destination.accept(newMessage.message);
                });

            } catch (InterruptedException e) {
                // thread interrupted
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void send(Message message, Router destination, float delay) {
        if (delay <= 0) {
            server.execute(() -> destination.accept(message));
            return;
        }

        queuedMessages.add(new DelayedMessage(message, destination, (long) (delay*1000f), TimeUnit.MILLISECONDS));
    }

    public void cancel(Predicate<DelayedMessage> criteria) {
        queuedMessages.removeIf(criteria);
    }
}
