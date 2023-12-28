package com.codinglitch.simpleradio.lexiconfig.classes;

import com.codinglitch.simpleradio.lexiconfig.Lexiconfig;
import com.electronwill.nightconfig.toml.TomlWriter;

public abstract class LexiconData {
    public LexiconData() {
        Lexiconfig.register(this);
    }
    public TomlWriter writer = new TomlWriter();
}
