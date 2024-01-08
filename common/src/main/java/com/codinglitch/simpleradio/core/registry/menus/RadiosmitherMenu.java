package com.codinglitch.simpleradio.core.registry.menus;

import com.codinglitch.simpleradio.core.central.Frequency;
import com.codinglitch.simpleradio.core.central.Receiving;
import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RadiosmitherMenu extends AbstractContainerMenu {
    private final Container container;

    public RadiosmitherMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(2));
    }

    public RadiosmitherMenu(int id, Inventory inventory, Container container) {
        super(SimpleRadioMenus.RADIOSMITHER_MENU, id);
        checkContainerSize(container, 2);
        this.container = container;

        container.startOpen(inventory.player);

        this.addSlot(new Slot(container, 0, 61, 46));
        this.addSlot(new Slot(container, 1, 61, 24));

        int m, l;
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(inventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(inventory, m, 8 + m * 18, 142));
        }
    }

    public void updateTinkering(String frequency, Frequency.Modulation modulation) {
        ItemStack tinkering = this.getTinkering();
        if (!tinkering.isEmpty() && tinkering.getItem() instanceof Receiving receiving) {
            receiving.setFrequency(tinkering, frequency, modulation);

            CompoundTag tag = tinkering.getOrCreateTag();
            if (tag.contains("user")) {
                tag.remove("user");
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (slotIndex < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public ItemStack getTinkering() {
        return container.getItem(0);
    }
}
