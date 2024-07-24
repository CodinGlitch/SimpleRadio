package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.central.WorldlyPosition;
import com.codinglitch.simpleradio.radio.RadioSpeaker;
import com.codinglitch.simpleradio.radio.RadioSource;

public interface CompatPlatform {
    void onData(RadioSpeaker channel, RadioSource source, short[] decoded);

    WorldlyPosition modifyPosition(WorldlyPosition position);
}