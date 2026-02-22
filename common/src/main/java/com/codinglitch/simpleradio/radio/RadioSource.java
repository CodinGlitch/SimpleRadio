package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.SimpleRadioLibrary;
import com.codinglitch.simpleradio.central.*;
import com.codinglitch.simpleradio.core.registry.FrequencingRegistry;
import com.codinglitch.simpleradio.core.registry.SimpleRadioFrequencing;
import com.codinglitch.simpleradio.core.registry.entities.Wire;
import com.codinglitch.simpleradio.routers.Router;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A source containing the audio data as well as other data collected while travelling.
 */
public class RadioSource implements Source {
    public UUID owner;
    public UUID originalOwner;
    public WorldlyPosition origin;
    public short frequencingType = -1;

    public byte[] data;
    public String sound;

    public float pitch = 1;
    public float volume;
    public float offset;
    public long seed;

    public float activity;

    public List<Short> record = new ArrayList<>();

    public Frequency frequencyMedium;
    public Wire wireMedium;

    public float transmissionCap = 50;
    public float transmissionPower = 50;

    protected RadioSource() {}

    public RadioSource(UUID owner, WorldlyPosition location, byte[] data, float volume) {
        this.owner = owner;
        this.origin = location;
        this.volume = volume;

        this.data = data;
    }

    public RadioSource(UUID owner, WorldlyPosition location, String sound, float volume) {
        this.owner = owner;
        this.origin = location;
        this.volume = volume;

        this.sound = sound;
    }

    public RadioSource(UUID owner, WorldlyPosition location, SoundEvent soundEvent, float volume) {
        this(owner, location, soundEvent.getLocation().toString(), volume);
    }

    @Override
    public float getActivity() {
        return activity;
    }

    @Override
    public float getPitch() {
        return pitch;
    }
    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public byte[] getData() {
        return data;
    }
    @Override
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String getSound() {
        return this.sound;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse(this.sound));
    }

    @Override
    public UUID getOwner() {
        return owner;
    }
    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public float getPower() {
        return transmissionPower;
    }
    @Override
    public void setPower(float transmissionPower) {
        this.transmissionPower = transmissionPower;
    }

    @Override
    public List<Short> getTravelRecord() {
        return record;
    }

    @Override
    public Frequency getFrequencyMedium() {
        return frequencyMedium;
    }
    @Override
    public void setFrequencyMedium(Frequency frequencyMedium) {
        this.frequencyMedium = frequencyMedium;
    }

    @Override
    public Wire getWireMedium() {
        return wireMedium;
    }
    @Override
    public void setWireMedium(Wiring wireMedium) {
        this.wireMedium = (Wire) wireMedium;
    }

    @Override
    public UUID getRealOwner() {
        return originalOwner == null ? owner : originalOwner;
    }

    @Override
    public void delegate(UUID owner) {
        this.originalOwner = this.owner;
        this.owner = owner;
    }

    @Override
    public void addPower(float power) {
        this.transmissionPower = Math.min(this.transmissionPower + power, this.transmissionCap);
    }

    @Override
    public FrequencingType getFrequencingType() {
        FrequencingType type = FrequencingRegistry.getById(this.frequencingType);
        if (type == null) {
            CommonSimpleRadio.error("Missing frequencing type for id {}!", this.frequencingType);
        }
        return type;
    }

    @Override
    public RadioSource copy() {
        RadioSource copy = new RadioSource();

        copy.owner = this.owner;
        copy.originalOwner = this.originalOwner;
        copy.origin = this.origin;
        copy.frequencingType = this.frequencingType;

        copy.data = this.data;
        copy.sound = this.sound;

        copy.volume = this.volume;
        copy.pitch = this.pitch;
        copy.offset = this.offset;
        copy.seed = this.seed;

        copy.record = new ArrayList<>(this.record);

        copy.frequencyMedium = this.frequencyMedium;
        copy.wireMedium = this.wireMedium;

        copy.transmissionPower = this.transmissionPower;
        copy.transmissionCap = this.transmissionCap;

        return copy;
    }

    @Override
    public boolean willShort(Router router) {
        short identifier = router.getIdentifier();
        for (short recordIdentifier : record) {
            if (identifier == recordIdentifier) return true;
        }
        return false;
    }

    @Override
    public void visit(Router router) {
        record.add(router.getIdentifier());
    }

    @Override
    public void travel(Router from, Router to, Medium medium) {
        WorldlyPosition fromPos = from.getLocation();
        WorldlyPosition toPos = to.getLocation();

        double distance = fromPos.distance(toPos);
        double transmissionDiminishment = 0;
        FrequencingType.DiminishmentMethod diminishmentMethod = FrequencingType.DiminishmentMethod.ADDITIVE;

        if (medium instanceof Wire wire) {
            transmissionDiminishment = SimpleRadioFrequencing.WIRE.transmissionDiminishment;
            diminishmentMethod = SimpleRadioFrequencing.WIRE.diminishmentMethod;

            this.wireMedium = wire;
        } else if (medium instanceof Frequency frequency) {
            FrequencingType type = this.getFrequencingType();
            transmissionDiminishment = type.transmissionDiminishment;
            diminishmentMethod = type.diminishmentMethod;

            if (to instanceof RadioReceiver receiver) {
                if (distance > receiver.frequencingType.receptionFloor) {
                    distance = Math.max(receiver.frequencingType.receptionFloor, distance - receiver.getPower());
                }
            }

            if (fromPos.level.dimensionType() != toPos.level.dimensionType()) {
                if (SimpleRadioLibrary.SERVER_CONFIG.frequency.crossDimensional) {
                    double interference = SimpleRadioLibrary.SERVER_CONFIG.frequency.dimensionalInterference;
                    transmissionDiminishment += frequency.getModulation() == Frequency.Modulation.FREQUENCY ? interference : interference/2;
                } else {
                    this.transmissionPower = 0;
                    transmissionDiminishment = 0;
                }
            }

            this.frequencyMedium = frequency;
        }

        // nevermind... dont beware.... negative transmission...
        switch (diminishmentMethod) {
            case ADDITIVE -> this.transmissionPower = (float) Math.max(0f, this.transmissionPower - (distance * transmissionDiminishment));
            case MULTIPLICATIVE -> this.transmissionPower = (float) Math.max(0f, this.transmissionPower - (this.transmissionCap * transmissionDiminishment));
        }

        this.visit(to);
    }

    @Override
    public double computeSeverity() {
        double base = 0;
        double severity = 0;
        if (this.frequencyMedium != null) {
            double diminishThreshold = this.getFrequencingType().getDiminishThreshold(frequencyMedium.getModulation());

            base = frequencyMedium.getModulation() == Frequency.Modulation.FREQUENCY ?
                    SimpleRadioLibrary.SERVER_CONFIG.frequency.baseFMInterference :
                    SimpleRadioLibrary.SERVER_CONFIG.frequency.baseAMInterference;
            severity = 1 - Math.clamp(0f, 1f,  this.transmissionPower / diminishThreshold);
        }

        return Math.clamp(
                0, 100,
                base + severity * (100 - base)
        );
    }

    public boolean isValid() {
        return this.origin != null;
    }
}
