package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.platform.services.RegistryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;

public class NeoForgeRegistryHelper implements RegistryHelper {
    public static Map<ResourceLocation, BlockEntityType<?>> BLOCK_ENTITIES = new HashMap<>();

    @Override
    public <BE extends BlockEntity> BlockEntityType<BE> registerBlockEntity(BlockEntityFactory<BE> factory, ResourceLocation resource, Block... blocks) {
        BlockEntityType<BE> blockEntityType = BlockEntityType.Builder.of((factory::create), blocks).build(null);
        BLOCK_ENTITIES.put(resource, blockEntityType);
        return blockEntityType;
    }
}