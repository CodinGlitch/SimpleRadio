package com.codinglitch.simpleradio.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface RegistryHelper {
    @FunctionalInterface
    interface BlockEntityFactory<BE extends BlockEntity> {
        BE create(BlockPos blockPos, BlockState blockState);
    }

    @FunctionalInterface
    interface MenuSupplier<M extends AbstractContainerMenu> {
        M create(int var1, Inventory var2);
    }

    <BE extends BlockEntity> BlockEntityType<BE> registerBlockEntity(BlockEntityFactory<BE> factory, ResourceLocation resource, Block... blocks);
    <M extends AbstractContainerMenu> MenuType<M> registerMenu(ResourceLocation resource, MenuSupplier<M> supplier);
    CreativeModeTab registerCreativeTab(ResourceLocation resource, CreativeModeTab creativeModeTab);
}