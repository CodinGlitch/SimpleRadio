package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.radio.CommonRadioPlugin;
import com.codinglitch.simpleradio.radio.RadioListener;
import com.codinglitch.simpleradio.radio.RadioSource;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public interface Listening extends Auditory {

    /**
     * Start listening in the world.
     * @param owner the Entity that will listen
     * @param id the UUID of the listener
     * @return The listener created.
     */
    default RadioListener startListening(Entity owner, @Nullable UUID id) {
        return setupListener(RadioListener.getOrCreateListener(owner, id));
    }
    /**
     * Start listening in the world.
     * @param location the location to listen to
     * @param id the UUID of the listener
     * @return The listener created.
     */
    default RadioListener startListening(WorldlyPosition location, @Nullable UUID id) {
        return setupListener(RadioListener.getOrCreateListener(location, id));
    }

    default RadioListener setupListener(RadioListener listener) {
        if (this instanceof CentralBlockEntity blockEntity) {
            listener.range = 12;
            listener.transformer(source -> {
                source.delegate(blockEntity.id);

                return source;
            });
        }

        return listener;
    }

    /**
     * Stop listening in the world.
     * @param owner the Entity that will stop listening
     */
    default void stopListening(Entity owner) {
        RadioListener.removeListener(owner);
    }

    /**
     * Stop listening in the world.
     * @param location the location of the listener to remove
     */
    default void stopListening(WorldlyPosition location) {
        RadioListener.removeListener(location);
    }

    /**
     * Stop listening in the world. Infers information from itself.
     */
    default void stopListening() {
        if (this instanceof CentralBlockEntity blockEntity) {
            if (blockEntity.listener != null) {
                stopListening(blockEntity.listener.location);
                blockEntity.listener.invalidate();
            }
        }
    }
}
