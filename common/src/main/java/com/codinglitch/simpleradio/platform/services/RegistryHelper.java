package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.core.networking.Packeter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface RegistryHelper {
    @FunctionalInterface
    interface BlockEntityFactory<BE extends BlockEntity> {
        BE create(BlockPos blockPos, BlockState blockState);
    }

    <BE extends BlockEntity> BlockEntityType<BE> registerBlockEntity(BlockEntityFactory<BE> factory, ResourceLocation resource, Block... blocks);
}