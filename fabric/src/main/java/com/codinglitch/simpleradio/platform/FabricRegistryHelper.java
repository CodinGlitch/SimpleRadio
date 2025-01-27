package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import com.codinglitch.simpleradio.platform.services.RegistryHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.function.Consumer;

public class FabricRegistryHelper implements RegistryHelper {

    @Override
    public <E extends Entity> EntityType<E> registerEntity(EntityType.EntityFactory<E> factory, MobCategory spawnGroup, Consumer<EntityType.Builder<E>> modifier, ResourceLocation resource) {
        EntityType.Builder<E> builder = EntityType.Builder.of(factory, spawnGroup);
        modifier.accept(builder);

        EntityType<E> entityType = Registry.register(
                BuiltInRegistries.ENTITY_TYPE, resource,
                builder.build(resource.getPath())
        );
        SimpleRadioEntities.ENTITIES.put(resource, entityType);
        return entityType;
    }

    @Override
    public <BE extends BlockEntity> BlockEntityType<BE> registerBlockEntity(BlockEntityFactory<BE> factory, ResourceLocation resource, Block... blocks) {
        BlockEntityType<BE> blockEntityType = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, resource,
                FabricBlockEntityTypeBuilder.create((factory::create), blocks).build()
        );
        SimpleRadioBlockEntities.BLOCK_ENTITIES.put(resource, blockEntityType);
        return blockEntityType;
    }

    @Override
    public <M extends AbstractContainerMenu> MenuType<M> registerMenu(ResourceLocation resource, MenuSupplier<M> supplier) {
        MenuType<M> menuType = Registry.register(BuiltInRegistries.MENU, resource, new MenuType<>(supplier::create, FeatureFlags.VANILLA_SET));
        SimpleRadioMenus.MENUS.put(resource, menuType);
        return menuType;
    }

    @Override
    public CreativeModeTab registerCreativeTab(ResourceLocation resource, CreativeModeTab creativeModeTab) {
        CreativeModeTab tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, resource, creativeModeTab);
        SimpleRadioMenus.CREATIVE_TABS.put(resource, tab);
        return tab;
    }
}
