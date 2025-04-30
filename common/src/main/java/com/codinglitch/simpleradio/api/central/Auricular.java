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
    static boolean validate(Entity entity, Class<?> clazz) {
        return RadioManager.verifyEntityCollection(entity, stack -> clazz.isAssignableFrom(stack.getItem().getClass()));
    }
}
