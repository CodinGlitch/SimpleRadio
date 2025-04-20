package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.core.registry.blocks.*;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;

import static com.codinglitch.simpleradio.CommonSimpleRadio.id;

public class SimpleRadioBlockEntities {
    public static Map<ResourceLocation, BlockEntityType<?>> BLOCK_ENTITIES = new HashMap<>();

    public static final BlockEntityType<RadiosmitherBlockEntity> RADIOSMITHER = Services.REGISTRY.registerBlockEntity(
            RadiosmitherBlockEntity::new, id("radiosmither"), SimpleRadioBlocks.RADIOSMITHER
    );

    public static final BlockEntityType<RadioBlockEntity> RADIO = Services.REGISTRY.registerBlockEntity(
            RadioBlockEntity::new, id("radio"), SimpleRadioBlocks.RADIO
    );
    public static final BlockEntityType<SpeakerBlockEntity> SPEAKER = Services.REGISTRY.registerBlockEntity(
            SpeakerBlockEntity::new, id("speaker"), SimpleRadioBlocks.SPEAKER
    );
    public static final BlockEntityType<MicrophoneBlockEntity> MICROPHONE = Services.REGISTRY.registerBlockEntity(
            MicrophoneBlockEntity::new, id("microphone"), SimpleRadioBlocks.MICROPHONE
    );

    public static final BlockEntityType<TransmitterBlockEntity> TRANSMITTER = Services.REGISTRY.registerBlockEntity(
            TransmitterBlockEntity::new, id("transmitter"), SimpleRadioBlocks.TRANSMITTER
    );
    public static final BlockEntityType<ReceiverBlockEntity> RECEIVER = Services.REGISTRY.registerBlockEntity(
            ReceiverBlockEntity::new, id("receiver"), SimpleRadioBlocks.RECEIVER
    );

    public static final BlockEntityType<InsulatorBlockEntity> INSULATOR = Services.REGISTRY.registerBlockEntity(
            InsulatorBlockEntity::new, id("socket"), SimpleRadioBlocks.INSULATOR
    );

    public static final BlockEntityType<FrequencerBlockEntity> FREQUENCER = Services.REGISTRY.registerBlockEntity(
            FrequencerBlockEntity::new, id("frequencer"), SimpleRadioBlocks.FREQUENCER
    );

    public static void load() {}
}
