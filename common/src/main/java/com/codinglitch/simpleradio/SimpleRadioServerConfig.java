package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.lexiconfig.annotations.Lexicon;
import com.codinglitch.simpleradio.lexiconfig.annotations.LexiconPage;
import com.codinglitch.simpleradio.lexiconfig.classes.LexiconData;
import com.codinglitch.simpleradio.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.lexiconfig.annotations.LexiconEntry;

@Lexicon(name = CommonSimpleRadio.ID+"-server")
public class SimpleRadioServerConfig extends LexiconData {
    @LexiconPage
    public Transceiver transceiver = new Transceiver();

    @LexiconPage
    public Transmitter transmitter = new Transmitter();

    @LexiconPage
    public Radio radio = new Radio();

    @LexiconPage
    public Frequency frequency = new Frequency();

    public static class Transceiver extends LexiconPageData {
        @LexiconEntry
        public Integer maxFMDistance = 350;

        @LexiconEntry
        public Integer falloffFM = 300;

        @LexiconEntry
        public Integer maxAMDistance = 550;

        @LexiconEntry
        public Integer falloffAM = 500;
    }

    public static class Transmitter extends LexiconPageData {
        @LexiconEntry
        public Integer maxFMDistance = 1100;

        @LexiconEntry
        public Integer falloffFM = 1000;

        @LexiconEntry
        public Integer maxAMDistance = 2200;

        @LexiconEntry
        public Integer falloffAM = 2000;
    }

    public static class Radio extends LexiconPageData {
        @LexiconEntry
        public Integer range = 24;
    }

    public static class Frequency extends LexiconPageData {
        @LexiconEntry
        public Integer wholePlaces = 3;

        @LexiconEntry
        public Integer decimalPlaces = 2;

        @LexiconEntry
        public String defaultFrequency = "auto-generate";

        @LexiconEntry
        public Boolean crossDimensional = false;

        @LexiconEntry
        public Integer dimensionalInterference = 10;

        @LexiconEntry
        public Integer packetBuffer = 2;
    }
}