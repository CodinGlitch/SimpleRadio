package com.codinglitch.simpleradio.core.central;

import com.codinglitch.simpleradio.client.core.central.AnimationInstance;
import net.minecraft.world.level.Level;

import java.util.Map;

public interface Animatable {
    Map<Integer, AnimationInstance> getStates();
    float getTime();
    void setTime(float time);

    default void allocate(int id) {
        getStates().put(id, new AnimationInstance());
    }

    default AnimationInstance getAnim(int id) {
        return getStates().get(id);
    }

    default AnimationInstance play(int id, float speed) {
        AnimationInstance instance = getAnim(id);

        return instance;
    }

    default void tick(Level level) {
        if (level.isClientSide) {
        }
    }
}
