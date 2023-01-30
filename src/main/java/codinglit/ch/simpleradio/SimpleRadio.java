package codinglit.ch.simpleradio;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SimpleRadio extends LoggingModInstance implements ModInitializer {
    static {
        ID = "simpleradio";
    }

    public static final Item RADIO = new RadioItem(new FabricItemSettings().group(ItemGroup.MISC).maxCount(1));

    public static SoundEvent RADIO_OPEN = new SoundEvent(new Identifier(ID, "radio_open"));
    public static SoundEvent RADIO_CLOSE = new SoundEvent(new Identifier(ID, "radio_close"));


    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(ID, "radio"), RADIO);

        Registry.register(Registry.SOUND_EVENT, new Identifier(ID, "radio_open"), RADIO_OPEN);
        Registry.register(Registry.SOUND_EVENT, new Identifier(ID, "radio_close"), RADIO_CLOSE);
    }
}
