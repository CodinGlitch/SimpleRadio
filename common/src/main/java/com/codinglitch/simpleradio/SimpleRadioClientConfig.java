package com.codinglitch.simpleradio;

import com.codinglitch.lexiconfig.annotations.Lexicon;
import com.codinglitch.lexiconfig.annotations.LexiconEntry;
import com.codinglitch.lexiconfig.annotations.LexiconPage;
import com.codinglitch.lexiconfig.classes.LexiconData;
import com.codinglitch.lexiconfig.classes.LexiconPageData;

@Lexicon(name = CommonSimpleRadio.ID+"-client")
public class SimpleRadioClientConfig extends LexiconData {
    @LexiconPage(comment = "These are the configurations for the wires.")
    public Wire wire = new Wire();

    public static class Wire extends LexiconPageData {
        @LexiconEntry(comment = "This determines whether or not wire effects will be processed. Defaults to true.")
        public Boolean effect = true;
        @LexiconEntry(comment = "This is the amount of time (in ticks) per block a wire effect lasts. Best if matching 'transmissionTime'. Defaults to 5.")
        public Double effectTime = 4d;
    }
}