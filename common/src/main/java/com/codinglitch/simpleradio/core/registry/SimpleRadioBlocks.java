package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.core.registry.blocks.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;

import java.util.HashMap;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioBlocks {
    public static final HashMap<ResourceLocation, Block> BLOCKS = new HashMap<>();

    public static RadiosmitherBlock RADIOSMITHER = (RadiosmitherBlock) register(id("radiosmither"), new RadiosmitherBlock(
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.DEEPSLATE)
    ));
    public static RadioBlock RADIO = (RadioBlock) register(id("radio"), new RadioBlock(
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static SpeakerBlock SPEAKER = (SpeakerBlock) register(id("speaker"), new SpeakerBlock(
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static MicrophoneBlock MICROPHONE = (MicrophoneBlock) register(id("microphone"), new MicrophoneBlock(
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static TransmitterBlock TRANSMITTER = (TransmitterBlock) register(id("transmitter"), new TransmitterBlock(
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));
    public static ReceiverBlock RECEIVER = (ReceiverBlock) register(id("receiver"), new ReceiverBlock(
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static FrequencerBlock FREQUENCER = (FrequencerBlock) register(id("frequencer"), new FrequencerBlock(
            BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)
    ));

    public static AntennaBlock ANTENNA = (AntennaBlock) register(id("antenna"), new AntennaBlock(
            BlockBehaviour.Properties.of(Material.METAL).strength(2.0F, 4.0F).sound(SoundType.METAL).instabreak()
    ));

    public static InsulatorBlock INSULATOR = (InsulatorBlock) register(id("insulator"), new InsulatorBlock(
            BlockBehaviour.Properties.of(Material.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD).instabreak()
    ));

    private static Block register(ResourceLocation location, Block block) {
        BLOCKS.put(location, block);
        return block;
    }
}
