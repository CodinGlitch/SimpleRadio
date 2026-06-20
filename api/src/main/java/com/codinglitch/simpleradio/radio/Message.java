package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.central.*;
import com.codinglitch.simpleradio.routers.Router;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;

import java.util.UUID;

/**
 * A message representing a packet for radio transmission containing the
 * audio data in a {@link Source} as well as other data collected while travelling.
 */
public interface Message {
    Source getSource();

    float getPitch();
    float getVolume();
    float getPower();
    float getOffset();

    UUID getOwner();
    UUID getRealOwner();

    long getSeed();

    ShortArrayList getTravelRecord();

    Frequency getFrequencyMedium();
    Wiring getWireMedium();
    FrequencingType getFrequencingType();

    WorldlyPosition getOrigin();

    float getActivity();

    void setPitch(float pitch);
    void setOwner(UUID owner);
    void setPower(float transmissionPower);
    void setFrequencyMedium(Frequency frequencyMedium);
    void setWireMedium(Wiring wireMedium);

    void delegate(UUID owner);

    void addPower(float power);

    Message copy();

    boolean willShort(Router router);
    double computeSeverity();

    void visit(Router router);
    void travel(Router from, Router to, Medium medium);
}
