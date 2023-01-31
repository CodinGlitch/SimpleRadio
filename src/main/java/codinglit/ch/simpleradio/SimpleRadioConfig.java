package codinglit.ch.simpleradio;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "simpleradio")
public class SimpleRadioConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip()
    public int maxRadioDistance = -1;
}
