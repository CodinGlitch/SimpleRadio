package com.codinglitch.simpleradio;

import com.codinglitch.lexiconfig.LexiconfigApi;
import com.codinglitch.lexiconfig.Library;
import com.codinglitch.lexiconfig.annotations.LexiconLibrary;
import com.codinglitch.simpleradio.api.central.Frequency;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.blocks.AntennaBlock;

@LexiconLibrary(name = CommonSimpleRadio.ID)
public class SimpleRadioLibrary extends Library {
    public static SimpleRadioServerConfig SERVER_CONFIG = new SimpleRadioServerConfig();
    public static SimpleRadioClientConfig CLIENT_CONFIG = new SimpleRadioClientConfig();

    @Override
    public void shelveLexicons() {
        LexiconfigApi.shelveLexicon(this, SERVER_CONFIG);
        LexiconfigApi.shelveLexicon(this, CLIENT_CONFIG);

        LexiconfigApi.registerListener(LexiconfigApi.EventType.POST_REVISION, (event) -> Frequency.onLexiconRevision());
        LexiconfigApi.registerListener(LexiconfigApi.EventType.POST_CATALOG, (event) -> Frequency.onLexiconRevision());

        LexiconfigApi.registerListener(LexiconfigApi.EventType.POST_REVISION, (event) -> AntennaBlock.onLexiconRevision());
        LexiconfigApi.registerListener(LexiconfigApi.EventType.POST_CATALOG, (event) -> AntennaBlock.onLexiconRevision());

        LexiconfigApi.registerListener(LexiconfigApi.EventType.POST_REVISION, (event) -> CompatCore.reloadCompatibilities());
        LexiconfigApi.registerListener(LexiconfigApi.EventType.POST_CATALOG, (event) -> CompatCore.spoutCompatibilities());

        LexiconfigApi.registerListener(LexiconfigApi.EventType.POST_REVISION, (event) -> SimpleRadioItems.reload());
        LexiconfigApi.registerListener(LexiconfigApi.EventType.POST_CATALOG, (event) -> SimpleRadioItems.reload());
    }
}
