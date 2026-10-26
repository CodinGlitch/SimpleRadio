package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.Executor;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Thread that handles the delivery of messages along routers with a delay not bounded by ticks.
 */
public class Courier extends Executor {
    public static int fatalTime = 0;
    public DelayQueue<DelayedMessage> queuedMessages = new DelayQueue<>();

    public Courier(MinecraftServer server) {
        super("Simple Radio Courier", server);
    }

    @Override
    public void run() {
        CommonSimpleRadio.info("Starting courier!");

        super.run();

        if (open) {
            CommonSimpleRadio.info("Courier was interrupted!");
        } else {
            CommonSimpleRadio.info("Closing courier!");
        }
    }

    @Override
    protected void execute() throws InterruptedException {
        DelayedMessage newMessage = queuedMessages.take();

        // relay the delayed message on the main thread
        server.execute(() -> {
            newMessage.destination.accept(newMessage.message);
        });
    }

    @Override
    public void tick() {
        fatalTime--;
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

    @Override
    public int getFatalTime() {
        return fatalTime;
    }

    public static Courier open(MinecraftServer server) {
        Courier courier = new Courier(server);
        courier.setDaemon(true);
        courier.start();
        return courier;
    }
}
