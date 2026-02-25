package com.codinglitch.simpleradio.core.registry.menus;

import com.codinglitch.simpleradio.central.Frequencing;
import com.codinglitch.simpleradio.central.Frequency;
import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import com.codinglitch.simpleradio.core.registry.blocks.RadiosmitherBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.codinglitch.simpleradio.core.SimpleRadioComponents.REFERENCE;

public class RadiosmitherMenu extends AbstractContainerMenu {
    private final Container container;

    public RadiosmitherMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(RadiosmitherBlockEntity.CONTAINER_SIZE));
    }

    public RadiosmitherMenu(int id, Inventory inventory, Container container) {
        super(SimpleRadioMenus.RADIOSMITHER_MENU, id);
        checkContainerSize(container, RadiosmitherBlockEntity.CONTAINER_SIZE);
        this.container = container;
        this.container.startOpen(inventory.player);

        this.addSlot(new Slot(this.container, 0, 71, 54) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof Frequencing;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                RadiosmitherMenu.this.slotsChanged(this.container);
            }
        });

        int m, l;
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(inventory, l + m * 9 + 9, 8 + l * 18, 102 + m * 18));
            }
        }
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(inventory, m, 8 + m * 18, 160));
        }
    }

    public void updateTinkering(String frequency, Frequency.Modulation modulation) {
        ItemStack tinkering = this.getTinkering();
        if (!tinkering.isEmpty() && tinkering.getItem() instanceof Frequencing frequencing) {
            frequencing.setFrequency(tinkering, frequency, modulation);

            if (tinkering.has(REFERENCE)) {
                tinkering.remove(REFERENCE);
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
