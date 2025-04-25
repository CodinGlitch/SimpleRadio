package com.codinglitch.simpleradio.core.registry.blocks;

import com.codinglitch.simpleradio.core.central.BaseContainer;
import com.codinglitch.simpleradio.core.registry.menus.RadiosmitherMenu;
import com.codinglitch.simpleradio.core.registry.SimpleRadioBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RadiosmitherBlockEntity extends BaseContainerBlockEntity {
    public static final int CONTAINER_SIZE = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

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
        ItemStack copy = stack.copy();
        copy.setCount(count);
        stack.shrink(count);
        return copy;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        ItemStack stack = items.get(i);
        ItemStack copy = stack.copy();
        stack.setCount(0);
        return copy;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        items.set(i, itemStack);
    }

    @Override
    public boolean stillValid(Player player) {
        Level level = this.getLevel();
        if (level == null) {
            return false;
        } else if (level.getBlockEntity(this.getBlockPos()) != this) {
            return false;
        } else {
            return player.distanceToSqr(Vec3.atCenterOf(this.getBlockPos())) <= 64d;
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected Component getDefaultName() {
        return null;
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
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, items);
        super.saveAdditional(tag);
    }

    @Override
    public void clearContent() {

    }
}
