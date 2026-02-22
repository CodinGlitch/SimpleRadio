package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.central.RouterHolder;
import com.codinglitch.simpleradio.central.WorldlyPosition;
import com.codinglitch.simpleradio.routers.Listener;

import java.util.Map;

public interface Listeners extends RouterHolder<Listener> {
    Map<Float, Listener> getAt(WorldlyPosition at);
}
