package codinglit.ch.simpleradio;

import codinglit.ch.simpleradio.registry.RadioItem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.plugins.impl.VoicechatConnectionImpl;
import de.maxhenkel.voicechat.voice.common.*;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerWorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Set;

public class RadioPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return SimpleRadio.ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(StaticSoundPacketEvent.class, this::onStaticSoundPacket);
    }

    public void onStaticSoundPacket(StaticSoundPacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection != null && connection.isInGroup()) {
            PlayerEntity player = (PlayerEntity) connection.getPlayer().getPlayer();
            Group group = connection.getGroup();
            if (player != null) {
                // Disable if sender is not using radio
                if (!(player.getActiveItem().getItem() instanceof RadioItem)) {
                    event.cancel();
                }

                // Disable if receiver has no radio or if not in range (when applicable)
                if (!event.isCancelled()) {
                    VoicechatConnection receivingConnection = event.getReceiverConnection();
                    if (receivingConnection instanceof VoicechatConnectionImpl voicechatConnection && receivingConnection.isInGroup()) {
                        PlayerEntity receivingPlayer = (PlayerEntity) voicechatConnection.getPlayer().getPlayer();
                        if (receivingPlayer != null) {
                            if (!receivingPlayer.getInventory().containsAny(Set.of(SimpleRadio.RADIO))) {
                                event.cancel();
                            } else if (SimpleRadio.CONFIG.maxRadioDistance != -1) {
                                if (!receivingPlayer.getBlockPos().isWithinDistance(player.getBlockPos(), SimpleRadio.CONFIG.maxRadioDistance)) {
                                    event.cancel();
                                }
                            }
                        }
                    }
                }

                // Broadcast proximity chat if not using radio
                if (event.isCancelled() && group.getType() == Group.Type.OPEN) {
                    // i cannot send custom sound packet or i'm dumb
                    // wish there was atleast function to generate packet so this is not hard coded
                    Server server = Voicechat.SERVER.getServer();
                    float distance = Utils.getDefaultDistance() * (player.isSneaking() ? Voicechat.SERVER_CONFIG.crouchDistanceMultiplier.get().floatValue() : 1F);
                    SoundPacket<?> soundPacket = new PlayerSoundPacket(player.getUuid(), event.getPacket().getOpusEncodedData(), event.getPacket().getSequenceNumber(), false, distance, null);

                    PlayerState senderState = server.getPlayerStateManager().getState(player.getUuid());

                    for (ServerPlayerEntity playerTo : ServerWorldUtils.getPlayersInRange((ServerWorld) player.getWorld(), player.getPos(), server.getBroadcastRange(distance), p -> !p.getUuid().equals(player.getUuid()))) {
                        PlayerState stateTo = server.getPlayerStateManager().getState(playerTo.getUuid());
                        if (stateTo == null)
                            continue;
                        if (stateTo.isDisabled() || stateTo.isDisconnected())
                            continue;
                        if (!stateTo.getGroup().equals(group.getId()))
                            continue;
                        // yes i literally just had to invert the group check
                        // sad that i can't do that with the api

                        ClientConnection clientConnection = server.getConnection(stateTo.getUuid());
                        if (clientConnection == null)
                            continue;

                        try {
                            server.sendSoundPacket((ServerPlayerEntity) player, senderState, playerTo, stateTo, clientConnection, soundPacket, SoundPacketEvent.SOURCE_PROXIMITY);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
