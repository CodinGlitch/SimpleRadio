package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.radio.Source;

import java.util.function.UnaryOperator;

/**
 * A type of {@link Router} that accepts {@link Source}s.
 * <br>
 * Often serves as the beginning of the audio pipeline.
 * <br>
 * <b>Does route further.</b>
 */
public interface Listener extends Router {

    float getRange();
    void setRange(float range);

    void transformer(UnaryOperator<Source> transformer);

    void listen(Source source);
}
