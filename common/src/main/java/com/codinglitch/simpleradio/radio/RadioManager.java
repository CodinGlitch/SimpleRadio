package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.*;

public class RadioManager {
    private static RadioManager INSTANCE;

    public static RadioManager getInstance() {
        if (INSTANCE == null) INSTANCE = new RadioManager();
        return INSTANCE;
    }

    public RadioManager() {}

    public static void serverTick(int tickCount) {
        if (tickCount % 20 == 0) {
            garbageCollect();
        }

        // -- Receiver, Transmitter and Listener ticking -- \\
        List<Frequency> frequencies = Frequency.getFrequencies();
        for (Frequency frequency : frequencies) {
            frequency.serverTick(tickCount);
            for (RadioTransmitter transmitter : frequency.transmitters) {
                transmitter.serverTick(tickCount);
            }
            for (RadioReceiver receiver : frequency.receivers) {
                receiver.serverTick(tickCount);
            }
        }

        for (RadioListener listener : RadioListener.getListeners()) {
            listener.serverTick(tickCount);
        }
        for (RadioSpeaker speaker : RadioSpeaker.getSpeakers()) {
            speaker.serverTick(tickCount);
        }
    }

    public static void garbageCollect() {
        Frequency.garbageCollect();
        RadioListener.garbageCollect();
        RadioSpeaker.garbageCollect();
    }

    public void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;

        ServerPlayer sender = (ServerPlayer) senderConnection.getPlayer().getPlayer();
        ServerLevel level = sender.serverLevel();

        //-- Get qualifying listeners
        TreeMap<Float, RadioListener> qualified = new TreeMap<>();
        for (RadioListener listener : RadioListener.getListeners()) {
            Vector3f position;
            if (listener.owner != null) {
                position = listener.owner.position().toVector3f();
            } else if (listener.location != null) {
                position = listener.location.position();
            } else continue;

            float distance = position.distanceSquared((float) sender.getX(), (float) sender.getY(), (float) sender.getZ());
            if (distance > listener.range) continue;

            qualified.put(distance, listener);
        }

        int listenedTo = 0;
        for (float distance : qualified.keySet()) {
            RadioListener listener = qualified.get(distance);

            float scale = 1f - (distance / listener.range);
            listener.onData(new RadioSource(
                    sender.getUUID(),
                    WorldlyPosition.of(sender.position().toVector3f(), level),
                    event.getPacket().getOpusEncodedData(),
                    scale
            ));

            listenedTo++;
            if (listenedTo >= SimpleRadioLibrary.SERVER_CONFIG.frequency.listenerBuffer) break;
        }

        ItemStack transceiver = sender.getUseItem();
        if (transceiver.getItem() instanceof TransceiverItem) {
            CompoundTag tag = transceiver.getOrCreateTag();
            Frequency frequency = Frequency.getOrCreateFrequency(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")));

            /*this.transmit(new RadioSource(
                    RadioSource.Type.TRANSCEIVER,
                    sender.getUUID(),
                    WorldlyPosition.of(sender.position().toVector3f(), level),
                    event.getPacket().getOpusEncodedData()
            ), frequency);*/
        }
    }
}
