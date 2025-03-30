package com.codinglitch.simpleradio.api.central;

import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.radio.RadioManager;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public interface Auricular {
    static boolean validate(WorldlyPosition position, Class<?> clazz) {
        return RadioManager.verifyLocationCollection(position, clazz);
    }
    static boolean validate(UUID uuid, Class<?> clazz) {
        VoicechatConnection connection = CommonRadioPlugin.serverApi.getConnectionOf(uuid);
        if (connection != null) return validate(connection, clazz);
        return false;
    }
    static boolean validate(VoicechatConnection connection, Class<?> clazz) {
        ServerPlayer player = (ServerPlayer) connection.getPlayer().getPlayer();
        if (player == null) return false;
        return validate(player, clazz);
    }
    static boolean validate(Entity entity, Class<?> clazz) {
        return RadioManager.verifyEntityCollection(entity, stack -> clazz.isAssignableFrom(stack.getItem().getClass()));
    }
}
