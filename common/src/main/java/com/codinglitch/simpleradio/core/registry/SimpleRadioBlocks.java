package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.core.registry.blocks.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;

import java.util.HashMap;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioBlocks {
    public static final HashMap<ResourceLocation, Block> BLOCKS = new HashMap<>();

    public static RadiosmitherBlock RADIOSMITHER = (RadiosmitherBlock) register(id("radiosmither"), new RadiosmitherBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE).pushReaction(PushReaction.IGNORE)
    ));
    public static RadioBlock RADIO = (RadioBlock) register(id("radio"), new RadioBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static SpeakerBlock SPEAKER = (SpeakerBlock) register(id("speaker"), new SpeakerBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static MicrophoneBlock MICROPHONE = (MicrophoneBlock) register(id("microphone"), new MicrophoneBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static TransmitterBlock TRANSMITTER = (TransmitterBlock) register(id("transmitter"), new TransmitterBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static ReceiverBlock RECEIVER = (ReceiverBlock) register(id("receiver"), new ReceiverBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static FrequencerBlock FREQUENCER = (FrequencerBlock) register(id("frequencer"), new FrequencerBlock(
            Block.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static AntennaBlock ANTENNA = (AntennaBlock) register(id("antenna"), new AntennaBlock(
            Block.Properties.of().strength(2.0F, 4.0F).sound(SoundType.METAL).instabreak()
    ));

    public static InsulatorBlock INSULATOR = (InsulatorBlock) register(id("insulator"), new InsulatorBlock(
            Block.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD).instabreak()
    ));

    private static Block register(ResourceLocation location, Block block) {
        BLOCKS.put(location, block);
        return block;
    }
}
