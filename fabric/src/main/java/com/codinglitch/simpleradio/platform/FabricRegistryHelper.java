package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.networking.Packeter;
import com.codinglitch.simpleradio.platform.services.NetworkingHelper;
import com.codinglitch.simpleradio.platform.services.RegistryHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FabricRegistryHelper implements RegistryHelper {

    @Override
    public <BE extends BlockEntity> BlockEntityType<BE> registerBlockEntity(BlockEntityFactory<BE> factory, ResourceLocation resource, Block... blocks) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, resource,
                FabricBlockEntityTypeBuilder.create((factory::create), blocks).build()
        );
    }
}
