package com.codinglitch.simpleradio.routers;

import com.codinglitch.simpleradio.radio.Source;

/**
 * A type of {@link Router} that accepts {@link Source}s and emits them in-world.
 * <br>
 * Often serves as the end of the audio pipeline.
 * <br>
 * <b>Does not route further.</b>
 */
public interface Speaker extends Router {

    float getRange();
    void setRange(float range);

    String getCategory();
    void setCategory(String category);

    int getSpeakingTime();
}
