package com.codinglitch.simpleradio.core.registry;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.core.registry.blocks.RadioBlockEntity;
import com.codinglitch.simpleradio.core.registry.blocks.RadiosmitherBlock;
import com.codinglitch.simpleradio.core.registry.blocks.RadiosmitherBlockEntity;
import com.codinglitch.simpleradio.platform.Services;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.TriConsumer;

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

    public static void load() {}
}
