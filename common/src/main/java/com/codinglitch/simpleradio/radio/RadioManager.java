package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
        if (transceiver.getItem() instanceof TransceiverItem) {
            CompoundTag tag = transceiver.getOrCreateTag();
            Frequency frequency = Frequency.getOrCreateFrequency(tag.getString("frequency"), Frequency.modulationOf(tag.getString("modulation")));

            this.transmit(new RadioSource(
                    RadioSource.Type.TRANSCEIVER,
                    sender.getUUID(),
                    WorldlyPosition.of(sender.position().toVector3f(), level),
                    event.getPacket().getOpusEncodedData()
            ), frequency);
        }


    }

    private void transmit(RadioSource source, Frequency frequency) {
        for (RadioChannel channel : frequency.listeners) {
            if (source.owner.equals(channel.owner)) continue;

            channel.transmit(source, frequency);
        }
    }
}
