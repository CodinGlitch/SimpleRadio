package com.codinglitch.simpleradio.platform.services;

import com.codinglitch.simpleradio.client.CommonSimpleRadioClient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public interface ClientRegistryHelper {
    interface ScreenConstructor<M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> {
        S create(M menu, Inventory inventory, Component component);
    }

    <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreen(MenuType<? extends M> menuType, ScreenConstructor<M, U> screenConstructor);
}