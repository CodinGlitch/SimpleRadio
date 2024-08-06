package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

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

    public enum CollectionResult {
        PASS,
        IGNORE,
        COLLECT
    }

    public static boolean verifyLocationCollection(WorldlyPosition position, Class<?> clazz) {
        BlockPos pos = position.realLocation();

        CollectionResult result = CompatCore.verifyLocationCollection(position, clazz);
        if (result == CollectionResult.IGNORE) {
            return true;
        } else if (result == CollectionResult.COLLECT) {
            return false;
        }

        if (!position.level.isLoaded(pos)) return false;

        BlockState state = position.level.getBlockState(pos);
        if (state.isAir()) return false;

        return clazz.isInstance(state.getBlock().asItem());
    }

    public static boolean verifyEntityCollection(Entity entity, Predicate<ItemStack> inventoryCriteria) {
        CollectionResult result = CompatCore.verifyEntityCollection(entity, inventoryCriteria);
        if (result == CollectionResult.IGNORE) {
            return true;
        } else if (result == CollectionResult.COLLECT) {
            return false;
        }

        if (entity instanceof Player player) {
            return player.getInventory().hasAnyMatching(inventoryCriteria);
        } else {
            for (ItemStack stack : entity.getHandSlots()) {
                if (inventoryCriteria.test(stack)) return true;
            }
            return false;
        }
    }

    @Nullable
    public static ItemStack isEntityHolding(Entity entity, Predicate<ItemStack> handCriteria) {
        for (ItemStack stack : entity.getHandSlots()) {
            if (handCriteria.test(stack)) return stack;
        }
        return null;
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
            if (listener.location != null) {
                position = listener.location.position();
            } else if (listener.owner != null) {
                position = listener.owner.position().toVector3f();
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
