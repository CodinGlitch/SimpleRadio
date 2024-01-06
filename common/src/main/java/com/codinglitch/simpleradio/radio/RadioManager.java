package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RadioManager {
    private static RadioManager INSTANCE;

    public static RadioManager getInstance() {
        if (INSTANCE == null) INSTANCE = new RadioManager();
        return INSTANCE;
    }
    public RadioManager() {
    }


    public void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;

        ServerPlayer sender = (ServerPlayer) senderConnection.getPlayer().getPlayer();
        ServerLevel level = sender.serverLevel();

        ItemStack transceiver = sender.getUseItem();
        if (!(transceiver.getItem() instanceof TransceiverItem)) return;
        CompoundTag tag = transceiver.getOrCreateTag();
        Frequency frequency = Frequency.getOrCreateFrequency(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")));

        transmit(level, frequency, sender.getUUID(), sender.position(), event.getPacket().getOpusEncodedData());
    }

    private void transmit(ServerLevel serverLevel, Frequency frequency, UUID sender, Vec3 senderLocation, byte[] opusEncodedData) {
        for (RadioChannel channel : frequency.listeners) {
            if (sender.equals(channel.owner)) continue;

            channel.transmit(sender, senderLocation, opusEncodedData);
        }
    }
}
