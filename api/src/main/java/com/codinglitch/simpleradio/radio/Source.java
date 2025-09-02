package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.central.FrequencingType;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.central.Medium;
import com.codinglitch.simpleradio.central.Wiring;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.sounds.SoundEvent;

import java.util.List;
import java.util.UUID;

public interface Source {
    byte[] getData();
    SoundEvent getSoundEvent();
    String getSound();

    float getPitch();
    UUID getOwner();
    float getPower();
    List<Short> getTravelRecord();
    Frequency getFrequencyMedium();
    Wiring getWireMedium();
    UUID getRealOwner();
    FrequencingType getFrequencingType();
    float getActivity();

    void setPitch(float pitch);
    void setData(byte[] data);
    void setOwner(UUID owner);
    void setPower(float transmissionPower);
    void setFrequencyMedium(Frequency frequencyMedium);
    void setWireMedium(Wiring wireMedium);

    void delegate(UUID owner);

    void addPower(float power);

    Source copy();

    boolean willShort(Router router);
    double computeSeverity();

    void visit(Router router);
    void travel(Router from, Router to, Medium medium);
}
