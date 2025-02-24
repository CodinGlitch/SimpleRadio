package com.codinglitch.simpleradio;

import com.codinglitch.lexiconfig.annotations.Lexicon;
import com.codinglitch.lexiconfig.annotations.LexiconEntry;
import com.codinglitch.lexiconfig.annotations.LexiconPage;
import com.codinglitch.lexiconfig.classes.LexiconData;
import com.codinglitch.lexiconfig.classes.LexiconPageData;

@Lexicon(name = CommonSimpleRadio.ID+"-server")
public class SimpleRadioServerConfig extends LexiconData {
    @LexiconPage(comment = "These are the configurations for the wires.")
    public Wire wire = new Wire();

    @LexiconPage(comment = "These are the configurations for the transceiver item.")
    public Transceiver transceiver = new Transceiver();

    @LexiconPage(comment = "These are the configurations for the walkie talkie item.")
    public WalkieTalkie walkie_talkie = new WalkieTalkie();

    @LexiconPage(comment = "These are the configurations for the receiver block.")
    public Receiver receiver = new Receiver();

    @LexiconPage(comment = "These are the configurations for the transmitter block.")
    public Transmitter transmitter = new Transmitter();

    @LexiconPage(comment = "These are the configurations for the radio block.")
    public Radio radio = new Radio();

    @LexiconPage(comment = "These are the configurations for the microphone block.")
    public Microphone microphone = new Microphone();

    @LexiconPage(comment = "These are the configurations for the speaker block.")
    public Speaker speaker = new Speaker();

    @LexiconPage(comment = "These are the configurations for the antenna block.")
    public Antenna antenna = new Antenna();

    @LexiconPage(comment = "These are the general configurations for frequencies.")
    public Frequency frequency = new Frequency();

    @LexiconPage(comment = "These are the general configurations for compatibilities.")
    public Compatibilities compatibilities = new Compatibilities();

    public static class Transceiver extends LexiconPageData {
        @LexiconEntry(comment = "This is how effective the transceiver is at receiving signals, and is essentially a flat bonus to transmission power. Defaults to 200.")
        public Integer receptionPower = 200;

        @LexiconEntry(comment = "This is the transmission power for frequency modulation. Defaults to 1000.")
        public Integer transmissionPowerFM = 1000;

        @LexiconEntry(comment = "This is the threshold of transmission power in frequency modulation at which it begins to have an auditory effect. Defaults to 200.")
        public Integer diminishThresholdFM = 200;

        @LexiconEntry(comment = "This is the range after which players can no longer be heard for amplitude modulation. Defaults to 1800.")
        public Integer transmissionPowerAM = 1800;

        @LexiconEntry(comment = "This is the threshold of transmission power in amplitude modulation at which it begins to have an auditory effect. Defaults to 300.")
        public Integer diminishThresholdAM = 300;

        @LexiconEntry(comment = "This is how much transmission power diminishes per block. Defaults to 1.")
        public Double transmissionDiminishment = 1d;

        @LexiconEntry(comment = "This is the range for the transceiver that it can hear from. Defaults to 4.")
        public Integer listeningRange = 4;
        @LexiconEntry(comment = "This is the range for the transceiver in which the audio played from it can be heard. Defaults to 4.")
        public Integer speakingRange = 4;

        @LexiconEntry(comment = "This is whether or not using the transceiver slows the player. Defaults to true.")
        public Boolean transceiverSlow = true;

        @LexiconEntry(comment = "When false, removes the transceiver recipe. Defaults to true.")
        public Boolean enabled = true;
    }

    public static class WalkieTalkie extends LexiconPageData {
        @LexiconEntry(comment = "This is how effective the walkie is at receiving signals, and is essentially a flat bonus to transmission power. Defaults to 100.")
        public Integer receptionPower = 100;

        @LexiconEntry(comment = "This is the transmission power for frequency modulation. Defaults to 500.")
        public Integer transmissionPowerFM = 500;

        @LexiconEntry(comment = "This is the threshold of transmission power in frequency modulation at which it begins to have an auditory effect. Defaults to 100.")
        public Integer diminishThresholdFM = 100;

        @LexiconEntry(comment = "This is the range after which players can no longer be heard for amplitude modulation. Defaults to 900.")
        public Integer transmissionPowerAM = 900;

        @LexiconEntry(comment = "This is the threshold of transmission power in amplitude modulation at which it begins to have an auditory effect. Defaults to 200.")
        public Integer diminishThresholdAM = 200;

        @LexiconEntry(comment = "This is how much transmission power diminishes per block. Defaults to 1.")
        public Double transmissionDiminishment = 1d;

        @LexiconEntry(comment = "This is the range for the walkie that it can hear from. Defaults to 4.")
        public Integer listeningRange = 4;
        @LexiconEntry(comment = "This is the range for the walkie in which the audio played from it can be heard. Defaults to 4.")
        public Integer speakingRange = 4;

        @LexiconEntry(comment = "This is whether or not using the walkie talkie slows the player. Defaults to true.")
        public Boolean walkieTalkieSlow = true;

        @LexiconEntry(comment = "When true, replaces the walkie talkie with the spuddie talkie. Defaults to true.")
        public Boolean spuddieTalkie = true;

        @LexiconEntry(comment = "When false, removes both the spuddie and walkie recipe. Defaults to true.")
        public Boolean enabled = true;
    }

    public static class Wire extends LexiconPageData {
        @LexiconEntry(comment = "This is how much transmission power diminishes per block. Defaults to 0.1.")
        public Double transmissionDiminishment = 0.1d;
        @LexiconEntry(comment = "This is the amount of time (in ticks) per block a wire takes to relay data. Defaults to 4.")
        public Double transmissionTime = 4d;

        @LexiconEntry(comment = "This is the amount of time (in ticks) between each header sent. Defaults to 5.")
        public Integer headerInterval = 5;

        @LexiconEntry(comment = "This is the range a wire can reach before breaking. Defaults to 12.")
        public Double range = 12d;

        @LexiconEntry(comment = "This is the amount of time (in ticks) a wire can survive while being invalid (no connections or out of range) before breaking. Defaults to 5.")
        public Integer invalidDeathTime = 5;

        @LexiconEntry(comment = "This is the amount of time (in ticks) between each effect for a wire. Can be disabled by setting to -1. Defaults to 5.")
        public Integer effectInterval = 5;
    }

    public static class Transmitter extends LexiconPageData {
        @LexiconEntry(comment = "This is the capability of this item to make use of antennas. Essentially acts as a multiplier for the antenna score. Defaults to 10.")
        public Integer antennaAptitude = 10;

        @LexiconEntry(comment = "This is the transmission power for frequency modulation. Defaults to 3000.")
        public Integer transmissionPowerFM = 3300;

        @LexiconEntry(comment = "This is the threshold of transmission power in frequency modulation at which it begins to have an auditory effect. Defaults to 300.")
        public Integer diminishThresholdFM = 300;

        @LexiconEntry(comment = "This is the range after which players can no longer be heard for amplitude modulation. Defaults to 4400.")
        public Integer transmissionPowerAM = 4400;

        @LexiconEntry(comment = "This is the threshold of transmission power in amplitude modulation at which it begins to have an auditory effect. Defaults to 500.")
        public Integer diminishThresholdAM = 500;

        @LexiconEntry(comment = "This is how much transmission power diminishes per block. Defaults to 1.")
        public Double transmissionDiminishment = 1d;

        @LexiconEntry(comment = "When false, removes the transmitter recipe. Defaults to true.")
        public Boolean enabled = true;
    }

    public static class Receiver extends LexiconPageData {
        @LexiconEntry(comment = "This is the capability of this item to make use of antennas. Essentially acts as a multiplier for the antenna score. Defaults to 10.")
        public Integer antennaAptitude = 10;

        @LexiconEntry(comment = "This is how effective the receiver is at receiving signals, and is essentially a flat bonus to transmission power. Defaults to 300.")
        public Integer receptionPower = 300;

        @LexiconEntry(comment = "When false, removes the receiver recipe. Defaults to true.")
        public Boolean enabled = true;
    }

    public static class Radio extends LexiconPageData {
        @LexiconEntry(comment = "This is how effective the radio is at receiving signals, and is essentially a flat bonus to transmission power. Defaults to 100.")
        public Integer receptionPower = 100;

        @LexiconEntry(comment = "This is the range for the radio in which the audio played from it can be heard. Defaults to 24.")
        public Integer speakingRange = 24;

        @LexiconEntry(comment = "When false, removes the radio recipe. Defaults to true.")
        public Boolean enabled = true;
    }

    public static class Microphone extends LexiconPageData {
        @LexiconEntry(comment = "This is the range for the microphone that it can hear from. Defaults to 8.")
        public Integer listeningRange = 8;

        @LexiconEntry(comment = "When false, removes the microphone recipe. Defaults to true.")
        public Boolean enabled = true;
    }

    public static class Speaker extends LexiconPageData {
        @LexiconEntry(comment = "This is the range for the radio in which the audio transmitted from it can be heard. Defaults to 32.")
        public Integer speakingRange = 32;

        @LexiconEntry(comment = "When false, removes the speaker recipe. Defaults to true.")
        public Boolean enabled = true;
    }

    public static class Antenna extends LexiconPageData {
        @LexiconEntry(comment = "This is the maximum distance an antenna can travel without support before falling. CAUTION: SETTING THIS TOO HIGH MAY CAUSE LAG WITH LARGE ANTENNAS. Defaults to 8.")
        public Integer maxDistance = 8;

        @LexiconEntry(comment = "When false, removes the antenna recipe. Defaults to true.")
        public Boolean enabled = true;
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

        @LexiconEntry(comment = "The base amount of interference to give to radio transmission per block across dimensions. Defaults to 4.")
        public Double dimensionalInterference = 4d;

        @LexiconEntry(comment = "The packet buffer for packet transmission. You likely won't need to worry about this. Defaults to 2.")
        public Integer packetBuffer = 2;

        @LexiconEntry(comment = "How many listeners should be able to receive a single players audio? Defaults to 2.")
        public Integer listenerBuffer = 20;

        @LexiconEntry(comment = "[EXPERIMENTAL] Pick up audio from the world, not just players. Defaults to false.")
        public Boolean soundListening = false;
    }

    public static class Compatibilities extends LexiconPageData {
        @LexiconPage(comment = "These are the configurations for the optional dependency Voice Chat Interaction.")
        public VoiceChatInteraction voice_chat_interaction = new VoiceChatInteraction();

        public static class VoiceChatInteraction extends LexiconPageData {
            @LexiconEntry(comment = "When false, removes compatibility for Voice Chat Interaction. Defaults to false. (NON-FUNCTIONAL)")
            public Boolean enabled = false;
        }

        //----

        @LexiconPage(comment = "These are the configurations for the optional dependency Vibrative Voice.")
        public VibrativeVoice vibrative_voice = new VibrativeVoice();

        public static class VibrativeVoice extends LexiconPageData {
            @LexiconEntry(comment = "When false, removes compatibility for Vibrative Voice. Defaults to true.")
            public Boolean enabled = true;
        }

        //----

        @LexiconPage(comment = "These are the configurations for the optional dependency Valkyrien Skies.")
        public ValkyrienSkies valkyrien_skies = new ValkyrienSkies();

        public static class ValkyrienSkies extends LexiconPageData {
            @LexiconEntry(comment = "When false, removes compatibility for Valkyrien Skies. Defaults to true.")
            public Boolean enabled = true;
        }

        //----

        @LexiconPage(comment = "These are the configurations for the optional dependency Create.")
        public Create create = new Create();

        public static class Create extends LexiconPageData {
            @LexiconEntry(comment = "When false, removes compatibility for Create. Defaults to true.")
            public Boolean enabled = true;
        }
    }
}