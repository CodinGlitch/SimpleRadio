package com.codinglitch.simpleradio.platform;

import com.codinglitch.simpleradio.core.central.ItemHolder;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioEntities;
import com.codinglitch.simpleradio.core.registry.SimpleRadioItems;
import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import com.codinglitch.simpleradio.platform.services.RegistryHelper;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FabricRegistryHelper implements RegistryHelper {

    @Override
    public <E extends Entity> EntityType<E> registerEntity(EntityType.EntityFactory<E> factory, MobCategory spawnGroup, Consumer<EntityType.Builder<E>> modifier, ResourceLocation resource) {
        EntityType.Builder<E> builder = EntityType.Builder.of(factory, spawnGroup);
        modifier.accept(builder);

        EntityType<E> entityType = Registry.register(
                Registry.ENTITY_TYPE, resource,
                builder.build(resource.getPath())
        );
        SimpleRadioEntities.ENTITIES.put(resource, entityType);
        return entityType;
    }

    @Override
    public <BE extends BlockEntity> BlockEntityType<BE> registerBlockEntity(BlockEntityFactory<BE> factory, ResourceLocation resource, Block... blocks) {
        BlockEntityType<BE> blockEntityType = Registry.register(
                Registry.BLOCK_ENTITY_TYPE, resource,
                FabricBlockEntityTypeBuilder.create((factory::create), blocks).build()
        );
        SimpleRadioBlockEntities.BLOCK_ENTITIES.put(resource, blockEntityType);
        return blockEntityType;
    }

    @Override
    public <M extends AbstractContainerMenu> MenuType<M> registerMenu(ResourceLocation resource, MenuSupplier<M> supplier) {
        MenuType<M> menuType = Registry.register(Registry.MENU, resource, new MenuType<>(supplier::create));
        SimpleRadioMenus.MENUS.put(resource, menuType);
        return menuType;
    }

    @Override
    public CreativeModeTab registerCreativeTab(ResourceLocation resource, Supplier<ItemStack> icon, Predicate<ItemHolder<?>> shouldAdd) {
        return FabricItemGroupBuilder.create(resource)
                .icon(icon)
                .appendItems((list) -> list.addAll(SimpleRadioItems.ITEMS
                        .values()
                        .stream()
                        .filter(shouldAdd)
                        .map(holder -> new ItemStack(holder.get()))
                        .toList()
                ))
                .build();
    }
}
