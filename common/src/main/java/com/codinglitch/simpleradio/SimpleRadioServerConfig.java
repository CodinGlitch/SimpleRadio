package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.lexiconfig.annotations.Lexicon;
import com.codinglitch.simpleradio.lexiconfig.annotations.LexiconPage;
import com.codinglitch.simpleradio.lexiconfig.classes.LexiconData;
import com.codinglitch.simpleradio.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.lexiconfig.annotations.LexiconEntry;

@Lexicon(name = "server-config")
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
        public int maxFMDistance = 350;

        @LexiconEntry
        public int falloffFM = 300;

        @LexiconEntry
        public int maxAMDistance = 550;

        @LexiconEntry
        public int falloffAM = 500;
    }

    public static class Transmitter extends LexiconPageData {
        @LexiconEntry
        public int maxFMDistance = 1100;

        @LexiconEntry
        public int falloffFM = 1000;

        @LexiconEntry
        public int maxAMDistance = 2200;

        @LexiconEntry
        public int falloffAM = 2000;
    }

    public static class Radio extends LexiconPageData {
        @LexiconEntry
        public int range = 24;
    }

    public static class Frequency extends LexiconPageData {
        @LexiconEntry
        public int wholePlaces = 3;

        @LexiconEntry
        public int decimalPlaces = 2;

        @LexiconEntry
        public boolean crossDimensional = false;

        @LexiconEntry
        public int dimensionalInterference = 10;

        @LexiconEntry
        public int packetBuffer = 2;
    }
}