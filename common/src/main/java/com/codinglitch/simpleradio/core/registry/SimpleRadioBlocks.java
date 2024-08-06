package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.core.registry.blocks.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

import java.util.HashMap;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioBlocks {
    public static final HashMap<ResourceLocation, Block> BLOCKS = new HashMap<>();

    public static Block RADIOSMITHER = register(id("radiosmither"), new RadiosmitherBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE)
    ));
    public static Block RADIO = register(id("radio"), new RadioBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static Block SPEAKER = register(id("speaker"), new SpeakerBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static Block MICROPHONE = register(id("microphone"), new MicrophoneBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static Block TRANSMITTER = register(id("transmitter"), new TransmitterBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static Block RECEIVER = register(id("receiver"), new ReceiverBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static Block FREQUENCER = register(id("frequencer"), new FrequencerBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static Block ANTENNA = register(id("antenna"), new AntennaBlock(
            Block.Properties.of().strength(2.0F, 4.0F).sound(SoundType.METAL).instabreak()
    ));

    private static Block register(ResourceLocation location, Block block) {
        BLOCKS.put(location, block);
        return block;
    }
}
