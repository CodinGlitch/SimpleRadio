package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RadiosmitherBlockEntity extends BaseContainerBlockEntity {
    public static final int CONTAINER_SIZE = 1;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public RadiosmitherBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleRadioBlockEntities.RADIOSMITHER, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RadiosmitherBlockEntity blockEntity) {

    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        ItemStack stack = items.get(i);
        ItemStack copy = stack.copyWithCount(count);
        stack.shrink(count);
        return copy;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return items.get(i).copyAndClear();
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        items.set(i, itemStack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected Component getDefaultName() {
        return null;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return createMenu(i, inventory);
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new RadiosmitherMenu(i, inventory, this);
    }

    public ItemStack tinkering() {
        return items.get(0);
    }

    public ItemStack applying() {
        return items.get(1);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ContainerHelper.loadAllItems(tag, items, provider);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        ContainerHelper.saveAllItems(tag, items, provider);
        super.saveAdditional(tag, provider);
    }

    @Override
    public void clearContent() {

    }
}
