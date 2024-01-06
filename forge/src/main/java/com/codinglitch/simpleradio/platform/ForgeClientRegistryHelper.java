package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.platform.services.ClientRegistryHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ForgeClientRegistryHelper implements ClientRegistryHelper {
    @Override
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreen(MenuType<? extends M> menuType, ScreenConstructor<M, U> screenConstructor) {
        MenuScreens.register(menuType, screenConstructor::create);
    }

    @Override
    public <BE extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends BE> blockEntity, BlockEntityRendererProvider<BE> rendererConstructor) {
        BlockEntityRenderers.register(blockEntity, rendererConstructor);
    }
}
