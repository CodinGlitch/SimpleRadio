package com.codinglitch.simpleradio;

import com.codinglitch.lexiconfig.LexiconfigApi;
import com.codinglitch.lexiconfig.annotations.Lexicon;
import com.codinglitch.lexiconfig.annotations.LexiconEntry;
import com.codinglitch.lexiconfig.annotations.LexiconPage;
import com.codinglitch.lexiconfig.classes.LexiconData;
import com.codinglitch.lexiconfig.classes.LexiconPageData;

@Lexicon(name = CommonSimpleRadio.ID+"-client", location = LexiconfigApi.Location.CLIENT)
public class SimpleRadioClientConfig extends LexiconData {
    @LexiconPage(comment = "These are the configurations for the wires.")
    public Wire wire = new Wire();

    @LexiconPage(comment = "These are the configurations for the transceiver item.")
    public Transceiver transceiver = new Transceiver();

    @LexiconPage(comment = "These are the configurations for the speaker block.")
    public Speaker speaker = new Speaker();

    @LexiconPage(comment = "These are the configurations for the microphone block.")
    public Microphone microphone = new Microphone();

    @LexiconPage(comment = "These are the configurations for the walkie talkie item.")
    public WalkieTalkie walkie_talkie = new WalkieTalkie();

    public static class Wire extends LexiconPageData {
        @LexiconEntry(comment = "This determines whether or not wire effects will be processed. Defaults to true.")
        public Boolean effect = true;

        @LexiconEntry(comment = "This is the amount of time (in ticks) per block a wire effect lasts. Best if matching 'transmissionTime'. Defaults to 4.")
        public Integer effectTime = 4;

        @LexiconEntry(comment = "This is how much wires drop from gravity. Defaults to 0.75.")
        public Double baseSag = 0.75d;
        @LexiconEntry(comment = "This is how much wires drop per block from gravity. Defaults to 0.075.")
        public Double distanceSag = 0.075d;
    }

    public static class Transceiver extends LexiconPageData {
        @LexiconEntry(comment = "This is whether or not using the transceiver slows the player. Defaults to true.")
        public Boolean transceiverSlow = true;
    }

    public static class Speaker extends LexiconPageData {
        @LexiconEntry(comment = "This is how often (in ticks) the speaker will emit a particle when active. Set to -1 to disable. Defaults to 10.")
        public Integer particleInterval = 10;
    }

    public static class Microphone extends LexiconPageData {
        @LexiconEntry(comment = "This is how often (in ticks) the microphone will emit a particle when active. Set to -1 to disable. Defaults to 10.")
        public Integer particleInterval = 10;
    }

    public static class WalkieTalkie extends LexiconPageData {
        @LexiconEntry(comment = "This is whether or not using the walkie talkie slows the player. Defaults to true.")
        public Boolean walkieTalkieSlow = true;
    }
}