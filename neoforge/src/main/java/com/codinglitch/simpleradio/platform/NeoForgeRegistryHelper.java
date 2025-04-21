package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import com.codinglitch.simpleradio.platform.services.RegistryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NeoForgeRegistryHelper implements RegistryHelper {

    @Override
    public <E extends Entity> EntityType<E> registerEntity(EntityType.EntityFactory<E> factory, MobCategory spawnGroup, Consumer<EntityType.Builder<E>> modifier, ResourceLocation resource) {
        EntityType.Builder<E> builder = EntityType.Builder.of(factory, spawnGroup);
        modifier.accept(builder);

        EntityType<E> entityType = builder.build(resource.getPath());
        SimpleRadioEntities.ENTITIES.put(resource, entityType);
        return entityType;
    }

    @Override
    public <BE extends BlockEntity> BlockEntityType<BE> registerBlockEntity(BlockEntityFactory<BE> factory, ResourceLocation resource, Block... blocks) {
        BlockEntityType<BE> blockEntityType = BlockEntityType.Builder.of((factory::create), blocks).build(null);
        SimpleRadioBlockEntities.BLOCK_ENTITIES.put(resource, blockEntityType);
        return blockEntityType;
    }

    @Override
    public <M extends AbstractContainerMenu> MenuType<M> registerMenu(ResourceLocation resource, MenuSupplier<M> supplier) {
        MenuType<M> menu = new MenuType<>((supplier::create), FeatureFlags.DEFAULT_FLAGS);
        SimpleRadioMenus.MENUS.put(resource, menu);
        return menu;
    }

    @Override
    public CreativeModeTab registerCreativeTab(ResourceLocation resource, CreativeModeTab creativeModeTab) {
        SimpleRadioMenus.CREATIVE_TABS.put(resource, creativeModeTab);
        return creativeModeTab;
    }
}