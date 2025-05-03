package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioManager;
import com.codinglitch.simpleradio.routers.Listener;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

import java.util.Map;
import java.util.UUID;

public interface ServerSimpleRadioApi extends SimpleRadioApi {
    static ServerSimpleRadioApi getInstance() {
        return RadioManager.getInstance();
    }

    Map<Float, Listener> getListeners(WorldlyPosition at);

    void sendSound(WorldlyPosition location, Holder<SoundEvent> soundHolder, float volume, float pitch, long seed);
    void sendSound(WorldlyPosition location, Holder<SoundEvent> soundHolder, float volume, float pitch, float offset, long seed);

    void sendAudio(WorldlyPosition location, UUID sender, byte[] data);
}
