package com.codinglitch.simpleradio.core.registry.menus;

import com.codinglitch.simpleradio.core.registry.SimpleRadioMenus;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RadiosmitherMenu extends AbstractContainerMenu {
    private final SimpleContainer container;

    public RadiosmitherMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(9));
    }

    public RadiosmitherMenu(int id, Inventory inventory, SimpleContainer container) {
        super(SimpleRadioMenus.RADIOSMITHER_MENU, id);
        checkContainerSize(container, 9);
        this.container = container;

        container.startOpen(inventory.player);

        //This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job
        int m;
        int l;
        //Our inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 3; ++l) {
                this.addSlot(new Slot(container, l + m * 3, 62 + l * 18, 17 + m * 18));
            }
        }
        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(inventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(inventory, m, 8 + m * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player var1, int var2) {
        return null;
    }

    @Override
    public boolean stillValid(Player var1) {
        return false;
    }
}
