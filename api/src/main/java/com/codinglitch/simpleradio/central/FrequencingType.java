package com.codinglitch.simpleradio.central;

import net.minecraft.resources.ResourceLocation;

public class FrequencingType {
    public enum DiminishmentMethod {
        ADDITIVE,
        MULTIPLICATIVE
    }

    public ResourceLocation location;
    public short id;

    public int receptionPower;
    public int receptionFloor;

    public int antennaAptitude;

    public int transmissionPowerFM;
    public int diminishThresholdFM;

    public int transmissionPowerAM;
    public int diminishThresholdAM;

    public DiminishmentMethod diminishmentMethod;
    public double transmissionDiminishment;

    public ConfigHolder page;

    public FrequencingType() {}

    public void reload() {
        if (page == null) return;
        this.receptionPower = (int) page.getEntry("receptionPower").orElse(-1);
        this.receptionFloor = (int) page.getEntry("receptionFloor").orElse(-1);

        this.antennaAptitude = (int) page.getEntry("antennaAptitude").orElse(-1);

        this.transmissionPowerFM = (int) page.getEntry("transmissionPowerFM").orElse(-1);
        this.diminishThresholdFM = (int) page.getEntry("diminishThresholdFM").orElse(-1);

        this.transmissionPowerAM = (int) page.getEntry("transmissionPowerAM").orElse(-1);
        this.diminishThresholdAM = (int) page.getEntry("diminishThresholdAM").orElse(-1);

        this.diminishmentMethod = DiminishmentMethod.valueOf((String) page.getEntry("diminishmentMethod").orElse("ADDITIVE"));
        this.transmissionDiminishment = (double) page.getEntry("transmissionDiminishment").orElse(-1d);
    }

    public int getTransmissionPower(Frequency.Modulation modulation) {
        return modulation == Frequency.Modulation.AMPLITUDE ? transmissionPowerAM : transmissionPowerFM;
    }
    public int getDiminishThreshold(Frequency.Modulation modulation) {
        return modulation == Frequency.Modulation.AMPLITUDE ? diminishThresholdAM : diminishThresholdFM;
    }
}
