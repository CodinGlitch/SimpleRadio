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

    public static class Transceiver extends LexiconPageData {
        @LexiconEntry
        public int maxTransceiverDistance = -1;
    }
}