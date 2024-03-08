package com.codinglitch.simpleradio;

import com.codinglitch.simpleradio.lexiconfig.annotations.Lexicon;
import com.codinglitch.simpleradio.lexiconfig.annotations.LexiconPage;
import com.codinglitch.simpleradio.lexiconfig.classes.LexiconData;
import com.codinglitch.simpleradio.lexiconfig.classes.LexiconPageData;
import com.codinglitch.simpleradio.lexiconfig.annotations.LexiconEntry;

@Lexicon(name = CommonSimpleRadio.ID+"-server")
public class SimpleRadioServerConfig extends LexiconData {
    @LexiconPage(comment = "This is the configurations for the transceiver item.")
    public Transceiver transceiver = new Transceiver();

    @LexiconPage(comment = "This is the configurations for the transmitter block. (IN DEVELOPMENT)")
    public Transmitter transmitter = new Transmitter();

    @LexiconPage(comment = "This is the configurations for the radio block.")
    public Radio radio = new Radio();

    @LexiconPage(comment = "This is the general configurations for frequencies.")
    public Frequency frequency = new Frequency();

    public static class Transceiver extends LexiconPageData {
        @LexiconEntry(comment = "This is the range after which players can no longer be heard for frequency modulation. Defaults to 700.")
        public Integer maxFMDistance = 700;

        @LexiconEntry(comment = "This is the range after which audio begins to decay for frequency modulation. Defaults to 600.")
        public Integer falloffFM = 600;

        @LexiconEntry(comment = "This is the range after which players can no longer be heard for amplitude modulation. Defaults to 1100.")
        public Integer maxAMDistance = 1100;

        @LexiconEntry(comment = "This is the range after which audio begins to decay for amplitude modulation. Defaults to 1000.")
        public Integer falloffAM = 1000;
    }

    public static class Transmitter extends LexiconPageData {
        @LexiconEntry(comment = "This is the range after which players can no longer be heard for frequency modulation. Defaults to 2200.")
        public Integer maxFMDistance = 2200;

        @LexiconEntry(comment = "This is the range after which audio begins to decay for frequency modulation. Defaults to 2000.")
        public Integer falloffFM = 2000;

        @LexiconEntry(comment = "This is the range after which players can no longer be heard for amplitude modulation. Defaults to 4400.")
        public Integer maxAMDistance = 4400;

        @LexiconEntry(comment = "This is the range after which audio begins to decay for amplitude modulation. Defaults to 4000.")
        public Integer falloffAM = 4000;
    }

    public static class Radio extends LexiconPageData {
        @LexiconEntry(comment = "This is the range for the radio in which the audio transmitted from it can be heard. Defaults to 24.")
        public Integer range = 24;
    }

    public static class Frequency extends LexiconPageData {
        @LexiconEntry(comment = "This is how many whole places (digits before the period) can exist in a frequency. Defaults to 3.")
        public Integer wholePlaces = 3;

        @LexiconEntry(comment = "This is how many decimal places (digits after the period) can exist in a frequency. Defaults to 2.")
        public Integer decimalPlaces = 2;

        @LexiconEntry(comment = "This is the default frequency to be provided to frequency-holding items. When set to auto-generate, will generate a pattern of zeros equal to the wholePlaces and decimalPlaces configurations, i.e. '000.00' by default. Defaults to auto-generate.")
        public String defaultFrequency = "auto-generate";

        @LexiconEntry(comment = "Whether or not the radios work across dimensions. Defaults to false.")
        public Boolean crossDimensional = false;

        @LexiconEntry(comment = "The base amount of interference to give to radio transmission across dimensions. Defaults to 10.")
        public Integer dimensionalInterference = 10;

        @LexiconEntry(comment = "The packet buffer for packet transmission. You likely won't need to worry about this. Defaults to 2.")
        public Integer packetBuffer = 2;
    }
}