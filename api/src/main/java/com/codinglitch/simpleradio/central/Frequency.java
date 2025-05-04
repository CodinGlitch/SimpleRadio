package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.routers.Receiver;
import com.codinglitch.simpleradio.routers.Transmitter;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface Frequency extends Medium {
    enum Modulation {
        FREQUENCY("FM"),
        AMPLITUDE("AM");

        public final String shorthand;

        Modulation(String shorthand) {
            this.shorthand = shorthand;
        }
    }

    String getFrequency();
    Modulation getModulation();

    int getIndex();

    List<Receiver> getReceivers();
    List<Transmitter> getTransmitters();

    //---- Receivers ----\\

    Receiver getReceiver(Predicate<Receiver> filter);
    Receiver getReceiver(WorldlyPosition location);
    Receiver getReceiver(Entity owner);
    Receiver getReceiver(UUID id);

    void registerReceiver(Receiver receiver);

    Receiver addReceiver(Receiver receiver);

    Receiver tryAddReceiver(UUID id, WorldlyPosition location);
    Receiver addReceiver(UUID id, WorldlyPosition location);

    Receiver tryAddReceiver(UUID id, Entity entity);
    Receiver addReceiver(UUID id, Entity entity);

    void removeReceiver(Predicate<Receiver> criteria);
    void removeReceiver(Receiver receiver);
    void removeReceiver(Entity owner);
    void removeReceiver(WorldlyPosition location);
    void removeReceiver(UUID id);

    //---- Transmitters ----\\

    Transmitter getTransmitter(Predicate<Transmitter> filter);
    Transmitter getTransmitter(WorldlyPosition location);
    Transmitter getTransmitter(Entity owner);
    Transmitter getTransmitter(UUID id);

    void registerTransmitter(Transmitter transmitter);

    Transmitter addTransmitter(Transmitter transmitter);

    Transmitter tryAddTransmitter(UUID id, WorldlyPosition location);
    Transmitter addTransmitter(UUID id, WorldlyPosition location);

    Transmitter tryAddTransmitter(UUID id, Entity entity);
    Transmitter addTransmitter(UUID id, Entity entity);

    void removeTransmitter(Predicate<Transmitter> criteria);
    void removeTransmitter(Transmitter transmitter);
    void removeTransmitter(Entity owner);
    void removeTransmitter(WorldlyPosition location);
    void removeTransmitter(UUID id);
}
