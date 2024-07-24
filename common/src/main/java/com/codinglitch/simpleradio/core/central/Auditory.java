package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public interface Auditory {
    static boolean validate(WorldlyPosition position, Class<? extends Auditory> clazz) {
        BlockPos pos = position.realLocation();

        if (!position.level.isLoaded(pos)) return false;

        BlockState state = position.level.getBlockState(pos);
        if (state.isAir()) return false;

        return clazz.isInstance(state.getBlock().asItem());
    }
    static boolean validate(UUID uuid, Class<? extends Auditory> clazz) {
        VoicechatConnection connection = CommonRadioPlugin.serverApi.getConnectionOf(uuid);
        if (connection != null) return validate(connection, clazz);
        return false;
    }
    static boolean validate(VoicechatConnection connection, Class<? extends Auditory> clazz) {
        ServerPlayer player = (ServerPlayer) connection.getPlayer().getPlayer();
        if (player == null) return false;
        return validate(player, clazz);
    }
    static boolean validate(Entity entity, Class<? extends Auditory> clazz) {
        return CommonRadioPlugin.verifyInventory(entity, stack -> stack.getItem().getClass().isInstance(clazz));
    }
}
