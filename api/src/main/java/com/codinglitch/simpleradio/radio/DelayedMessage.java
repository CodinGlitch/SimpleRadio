package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.routers.Router;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedMessage implements Delayed {
    public final Message message;
    public final Router destination;
    private final long relaysAt;

    DelayedMessage(Message message, Router destination, long delay, TimeUnit unit) {
        this.message = message;
        this.destination = destination;
        this.relaysAt = System.currentTimeMillis() + unit.toMillis(delay);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(timeLeft(), TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the amount of time left in seconds.
     * @return The time left
     */
    public float secondsLeft() {
        return TimeUnit.MILLISECONDS.toSeconds(timeLeft());
    }

    /**
     * Gets the amount of time left in milliseconds.
     * @return The time left
     */
    public long timeLeft() {
        return relaysAt - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(
                this.getDelay(TimeUnit.MILLISECONDS),
                o.getDelay(TimeUnit.MILLISECONDS)
        );
    }
}