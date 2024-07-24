package com.codinglitch.simpleradio.compat;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;
import de.maxhenkel.vcinteraction.AudioUtils;
import de.maxhenkel.vcinteraction.VoicechatInteraction;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InteractionCompat {
    private static ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();;

    public static void onData(RadioSpeaker channel, RadioSource source, short[] decodedData) {
        UUID sourceOwner = source.getRealOwner();
        VoicechatConnection connection = CommonRadioPlugin.serverApi.getConnectionOf(sourceOwner);

        if (AudioUtils.calculateAudioLevel(decodedData) < VoicechatInteraction.SERVER_CONFIG.minActivationThreshold.get().doubleValue()) {
            return;
        }

        if (connection == null) {
            if (channel.location == null) return;

            WorldlyPosition location = channel.location;
            location.level.getServer().execute(() -> {
                if (setCooldown(channel.owner, location.level)) {
                    BlockState state = location.level.getBlockState(location.blockPos());

                    location.level.gameEvent(VoicechatInteraction.VOICE_GAME_EVENT, location.blockPos(), GameEvent.Context.of(state));
                }
            });
        } else {
            if (!(connection.getPlayer().getPlayer() instanceof ServerPlayer player)) {
                CommonSimpleRadio.warn("Received microphone packets from non-player");
                return;
            }

            if (setCooldown(player.getUUID(), player.level())) {
                player.gameEvent(VoicechatInteraction.VOICE_GAME_EVENT);
            }
        }
    }

    private static boolean setCooldown(UUID uuid, Level level) {
        Long lastTimestamp = cooldowns.get(uuid);
        long currentTime = level.getGameTime();
        if (lastTimestamp == null || currentTime - lastTimestamp > 20L) {
            cooldowns.put(uuid, currentTime);
            return true;
        }
        return false;
    }
}
