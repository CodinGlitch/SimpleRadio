package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import net.minecraft.server.MinecraftServer;

public abstract class Executor extends Thread {
    public enum MonitorState {
        ALIVE,
        DEAD,
        FATAL
    }

    protected final MinecraftServer server;

    protected volatile boolean open = true;

    public Executor(String name, MinecraftServer server) {
        super(name);
        this.server = server;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && open) {
            try {
                this.execute();
            } catch (InterruptedException e) {
                // thread interrupted
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public MonitorState monitor() {
        if (!this.isAlive()) {
            if (getFatalTime() > 500) {
                CommonSimpleRadio.error("The courier has been fatally stopped due to throwing too many exceptions!");

                return MonitorState.FATAL;
            } else {
                return MonitorState.DEAD;
            }
        }

        return MonitorState.ALIVE;
    }

    public abstract int getFatalTime();

    public void close() {
        open = false;
        this.interrupt();
    }

    protected abstract void execute() throws InterruptedException;
    public abstract void tick();
}